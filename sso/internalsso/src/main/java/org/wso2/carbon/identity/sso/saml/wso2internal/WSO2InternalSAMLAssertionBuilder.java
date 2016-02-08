package org.wso2.carbon.identity.sso.saml.wso2internal;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.ActionBuilder;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.AuthzDecisionStatementBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.builders.SignKeyDataHolder;
import org.wso2.carbon.identity.sso.saml.builders.assertion.DefaultSAMLAssertionBuilder;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class WSO2InternalSAMLAssertionBuilder extends DefaultSAMLAssertionBuilder {

    private static Log log = LogFactory.getLog(WSO2InternalSAMLAssertionBuilder.class);

    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";

    private String userAttributeSeparator = ",";

    @Override
    public Assertion buildAssertion(SAMLSSOAuthnReqDTO authReqDTO, DateTime notOnOrAfter, String sessionId)
            throws IdentityException {

        if (log.isDebugEnabled()) {
            log.debug("Invoked the WSO2 InternalApp SSO Assertion Builder ");
        }

        try {
            DateTime currentTime = new DateTime();
            Assertion samlAssertion = new AssertionBuilder().buildObject();
            samlAssertion.setID(SAMLSSOUtil.createID());
            samlAssertion.setVersion(SAMLVersion.VERSION_20);
            samlAssertion.setIssuer(SAMLSSOUtil.getIssuer());
            samlAssertion.setIssueInstant(currentTime);
            Subject subject = new SubjectBuilder().buildObject();

            NameID nameId = new NameIDBuilder().buildObject();

            if (authReqDTO.getUseFullyQualifiedUsernameAsSubject()) {
                nameId.setValue(authReqDTO.getUsername());
                if (authReqDTO.getNameIDFormat() != null) {
                    nameId.setFormat(authReqDTO.getNameIDFormat());
                } else {
                    nameId.setFormat(NameIdentifier.EMAIL);
                }
            } else {
                // get tenant domain name from the username
                String tenantDomainFromUserName = MultitenantUtils.getTenantDomain(authReqDTO.getUsername());
                String authenticatedUserTenantDomain = SAMLSSOUtil.getUserTenantDomain();

                if (authenticatedUserTenantDomain == null
                        || !authenticatedUserTenantDomain.equals(tenantDomainFromUserName)) {
                    // this means username comes from a federated Idp. no local
                    // authenticator used.
                    // no asserted identity for the user.
                    nameId.setValue(authReqDTO.getUsername());
                } else {
                    nameId.setValue(MultitenantUtils.getTenantAwareUsername(authReqDTO.getUsername()));
                }

                nameId.setFormat(authReqDTO.getNameIDFormat());
            }
            nameId.setValue(changeEmailForConcurUSD(authReqDTO.getQueryString(), nameId.getValue()));

            subject.setNameID(nameId);

            SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder().buildObject();
            subjectConfirmation.setMethod(SAMLSSOConstants.SUBJECT_CONFIRM_BEARER);
            SubjectConfirmationData scData = new SubjectConfirmationDataBuilder().buildObject();
            scData.setRecipient(authReqDTO.getAssertionConsumerURL());
            scData.setNotOnOrAfter(notOnOrAfter);
            if (!authReqDTO.isIdPInitSSO()) {
                scData.setInResponseTo(authReqDTO.getId());
            }
            subjectConfirmation.setSubjectConfirmationData(scData);
            subject.getSubjectConfirmations().add(subjectConfirmation);

            if (authReqDTO.getRequestedRecipients() != null && authReqDTO.getRequestedRecipients().length > 0) {
                for (String recipient : authReqDTO.getRequestedRecipients()) {
                    subjectConfirmation = new SubjectConfirmationBuilder().buildObject();
                    subjectConfirmation.setMethod(SAMLSSOConstants.SUBJECT_CONFIRM_BEARER);
                    scData = new SubjectConfirmationDataBuilder().buildObject();
                    scData.setRecipient(recipient);
                    scData.setNotOnOrAfter(notOnOrAfter);
                    if (!authReqDTO.isIdPInitSSO()) {
                        scData.setInResponseTo(authReqDTO.getId());
                    }
                    subjectConfirmation.setSubjectConfirmationData(scData);
                    subject.getSubjectConfirmations().add(subjectConfirmation);
                }
            }

            samlAssertion.setSubject(subject);

            AuthnStatement authStmt = new AuthnStatementBuilder().buildObject();
            authStmt.setAuthnInstant(new DateTime());

            AuthnContext authContext = new AuthnContextBuilder().buildObject();
            AuthnContextClassRef authCtxClassRef = new AuthnContextClassRefBuilder().buildObject();
            authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
            authContext.setAuthnContextClassRef(authCtxClassRef);
            authStmt.setSessionNotOnOrAfter(notOnOrAfter);
            authStmt.setAuthnContext(authContext);
            if (authReqDTO.isDoSingleLogout()) {
                authStmt.setSessionIndex(sessionId);
            }
            samlAssertion.getAuthnStatements().add(authStmt);

            /*
             * If <AttributeConsumingServiceIndex> element is in the <AuthnRequest> and according to
             * the spec 2.0 the subject MUST be in the assertion
             */
            Map<String, String> claims = SAMLSSOUtil.getAttributes(authReqDTO);
            if (claims != null && !claims.isEmpty()) {
                AttributeStatement attrStmt = buildAttributeStatement(claims, authReqDTO);
                if (attrStmt != null) {
                    samlAssertion.getAttributeStatements().add(attrStmt);
                }
            }

            AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
            Audience issuerAudience = new AudienceBuilder().buildObject();
            issuerAudience.setAudienceURI(authReqDTO.getIssuerWithDomain());
            audienceRestriction.getAudiences().add(issuerAudience);
            if (authReqDTO.getRequestedAudiences() != null) {
                for (String requestedAudience : authReqDTO.getRequestedAudiences()) {
                    Audience audience = new AudienceBuilder().buildObject();
                    audience.setAudienceURI(requestedAudience);
                    audienceRestriction.getAudiences().add(audience);
                }
            }
            Conditions conditions = new ConditionsBuilder().buildObject();
            conditions.setNotBefore(currentTime);
            conditions.setNotOnOrAfter(notOnOrAfter);
            conditions.getAudienceRestrictions().add(audienceRestriction);
            samlAssertion.setConditions(conditions);

            insertValuesForConcurEmail(samlAssertion, authReqDTO, notOnOrAfter, sessionId);

            if (authReqDTO.getDoSignAssertions()) {
                SAMLSSOUtil.setSignature(samlAssertion, XMLSignature.ALGO_ID_SIGNATURE_RSA, new SignKeyDataHolder(
                        authReqDTO.getUsername()));
            }

            return samlAssertion;
        } catch (Exception e) {
            log.error("Error when reading claim values for generating SAML Response", e);
            throw new IdentityException("Error when reading claim values for generating SAML Response", e);
        }
    }

    private String changeEmailForConcurUSD(String query, String nameID) throws IdentityException {
        if (query != null && query.trim().length() > 0 && query.startsWith("spEntityID=concurUSD")) {
            nameID = nameID.replaceAll("@wso2.com", "usd@wso2.com");
        }
        return nameID;
    }

    private Assertion insertValuesForConcurEmail(Assertion assertion, SAMLSSOAuthnReqDTO authReqDTO,
            DateTime notOnOrAfter, String sessionId) throws IdentityException {

        String query = authReqDTO.getQueryString();

        if (query != null && query.trim().length() > 0 && query.startsWith("spEntityID=concur")) {

            String hpo = null;
            String cte = null;
            String[] parts = query.split("&");

            if (parts.length == 3) {

                if (log.isDebugEnabled()) {
                    log.debug("Generating asseration for Concur with hpo and cte values. For Email logins.");
                }

                if (parts[1].startsWith("hpo")) {
                    hpo = parts[1].substring(parts[1].indexOf("=") + 1);
                }
                if (parts[2].startsWith("cte")) {
                    cte = parts[2].substring(parts[2].indexOf("=") + 1);
                }

                AuthzDecisionStatement authzStmt = new AuthzDecisionStatementBuilder().buildObject();
                authzStmt.setResource(cte);

                Action action = (Action) new ActionBuilder().buildObject();
                action.setAction(hpo); // hpo is action

                authzStmt.getActions().add(action);
                assertion.getAuthzDecisionStatements().add(authzStmt);

                if (log.isDebugEnabled()) {
                    try {
                        AssertionMarshaller marshaller = new AssertionMarshaller();
                        Element element = marshaller.marshall(assertion);
                        log.debug("SAMLe assertion is " + XMLHelper.prettyPrintXML(element));
                    } catch (MarshallingException e) {
                        log.debug("Error marshelling SAML" + e.getMessage(), e);
                    }
                }
            }
        }
        return assertion;
    }

    private AttributeStatement buildAttributeStatement(Map<String, String> claims, SAMLSSOAuthnReqDTO authReqDTO) {

        String claimSeparator = claims.get(MULTI_ATTRIBUTE_SEPARATOR);
        if (claimSeparator != null) {
            userAttributeSeparator = claimSeparator;
            claims.remove(MULTI_ATTRIBUTE_SEPARATOR);
        }

        AttributeStatement attStmt = new AttributeStatementBuilder().buildObject();
        Iterator<String> ite = claims.keySet().iterator();
        boolean atLeastOneNotEmpty = false;

        for (int i = 0; i < claims.size(); i++) {
            String claimUri = ite.next();
            String claimValue = claims.get(claimUri);
            if (claimUri != null && !claimUri.trim().isEmpty() && claimValue != null && !claimValue.trim().isEmpty()) {
                atLeastOneNotEmpty = true;
                Attribute attrib = new AttributeBuilder().buildObject();
                attrib.setName(claimUri);
                // setting NAMEFORMAT attribute value to basic attribute profile
                attrib.setNameFormat(SAMLSSOConstants.NAME_FORMAT_BASIC);
                // look
                // https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUsrManJavaAnyTypes
                XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory().getBuilder(
                        XSString.TYPE_NAME);
                XSString stringValue;

                // Need to check if the claim has multiple values
                if (userAttributeSeparator != null && claimValue.contains(userAttributeSeparator)) {
                    StringTokenizer st = new StringTokenizer(claimValue, userAttributeSeparator);
                    while (st.hasMoreElements()) {
                        String attValue = st.nextElement().toString();
                        if (attValue != null && attValue.trim().length() > 0) {
                            stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                                    XSString.TYPE_NAME);
                            stringValue.setValue(attValue);
                            attrib.getAttributeValues().add(stringValue);
                        }
                    }
                } else {
                    stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
                    stringValue.setValue(claimValue);
                    attrib.getAttributeValues().add(stringValue);
                }

                attStmt.getAttributes().add(attrib);
            }
        }
        if (atLeastOneNotEmpty) {
            return attStmt;
        } else {
            return null;
        }
    }

}