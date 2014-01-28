/**
 * 
 */
package com.moedamas.busdriver.modules.mail;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.core.ConfigMgr;
import com.moedamas.busdriver.modules.busAbstractModule;

/**
 * @author cemartins
 *
 */
public class busMail extends busAbstractModule {
	
	javax.mail.Session MailSession = null;
	String mailMimeCharset;
	Properties mailProperties = new Properties();

	/**
	 * @param moduleRef
	 * @param config
	 */
	public busMail(String moduleRef, ConfigMgr config) throws busException {
		super(moduleRef, config);

		String mailProperty;

		mailMimeCharset = config.getStringValue("mail.mime.charset");
		if(mailMimeCharset == null)
			throw new busException("busSendMail: property mail.mime.charset not found in the busDriver.properties file.");
		mailProperties.put("mail.mime.charset", mailMimeCharset);
		mailProperty = config.getStringValue("mail.store.protocol");
		if(mailProperty == null)
			throw new busException("busSendMail: property mail.store.protocol not found in the busDriver.properties file.");
		mailProperties.put("mail.store.protocol", mailProperty);
		mailProperty = config.getStringValue("mail.transport.protocol");
		if(mailProperty == null)
			throw new busException("busSendMail: property mail.transport.protocol not found in the busDriver.properties file.");
		mailProperties.put("mail.transport.protocol", mailProperty);
		mailProperty = config.getStringValue("mail.smtp.host");
		if(mailProperty == null)
			throw new busException("busSendMail: property mail.smtp.host not found in the busDriver.properties file.");
		mailProperties.put("mail.smtp.host", mailProperty);
		mailProperty = config.getStringValue("mail.smpt.user");
		if(mailProperty == null)
			throw new busException("busSendMail: property mail.smpt.user not found in the busDriver.properties file.");
		mailProperties.put("mail.smpt.user", mailProperty);
		mailProperty = config.getStringValue("mail.from");
		if(mailProperty == null)
			throw new busException("busSendMail: property mail.from not found in the busDriver.properties file.");
		mailProperties.put("mail.from", mailProperty);
		mailProperty = config.getStringValue("mail.debug");
		if(mailProperty == null)
			throw new busException("busSendMail: property mail.debug not found in the busDriver.properties file.");
		mailProperties.put("mail.debug", mailProperty);

}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#prepareContext(javax.servlet.ServletContext)
	 */
	@Override
	public void prepareContext(ServletContext context) throws busException {

		// Create a new mail session
		MailSession = javax.mail.Session.getInstance(mailProperties);
		if(MailSession == null)
			throw new busException("busSendMail: cannot obtain mail session.");

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#finishContext()
	 */
	@Override
	public void finishContext() throws busException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#prepareService(com.moedamas.busdriver.core.busDriverRequest)
	 */
	@Override
	public void prepareService(busDriverRequest request) throws busException {

		request.addValue(moduleRef, this);

	}

	/* (non-Javadoc)
	 * @see com.moedamas.busdriver.modules.busModule#finishService(com.moedamas.busdriver.core.busDriverRequest)
	 */
	@Override
	public void finishService(busDriverRequest request, boolean success) throws busException {
		// TODO Auto-generated method stub

	}

	/**
	 * Sends a HTML formated message using the following attributes in the requestData:
	 * <ul type="square">
	 * <li>MAILFROMNAME - Name of the sender.
	 * <li>MAILFROM - Internet e-mail address of the sender (1 item). If null will use the mail.from attribute of the busDriver.properties file.
	 * <li>MAILTO   - Internet e-mail adresses of the receivers (1 to n). caanot be null
	 * <li>MAILCC   - Internet e-mail adresses of the receivers (1 to n).
	 * <li>MAILBCC  - Internet e-mail adresses of the receivers (1 to n).
	 * <li>MAILSUBJECT - Subject of the e-mail message to send
	 * <li>MAILCONTENT - The message content in HTML format
	 * </ul>
	 * if the message cannot be sent, the following attributes are added to the requestData:
	 * <ul type="square">
	 * <li>MAILMSG - The error message.
	 * <li>MAILVALIDSENTADDRESSES   - the addresses to which this message was sent succesfully.
	 * <li>MAILVALIDUNSENTADDRESSES - the addresses that are valid but to which this message was not sent.
	 * <li>MAILINVALIDADDRESSES     - the addresses to which this message could not be sent.
	 * </ul>
	 * @param   requestData		The jtpDataStructure of the service calling this method.
	 * @return <code>true</code> if everything goes well, otherwise returns <code>false</code>, or throws a busException.
	 * @throws busException		if an error occurs while trying to send the mail message.
	 */
	public boolean sendHTML(busDriverRequest requestData) throws busException	{
		javax.mail.Message mailMsg;
		DataHandler ds;
		StringBuffer receipientAddresses;
		Vector mailTo, mailCc, mailBcc;
		String mailFromName, mailFrom, mailSubject, mailReceiver, mailContent;
		boolean result = true;

	    	// initialize the mail message
	    	mailMsg = new javax.mail.internet.MimeMessage(MailSession);

	    	// get data from the request
	    	mailSubject = (String)requestData.getValue("MAILSUBJECT");
	    	mailFrom = (String)requestData.getValue("MAILFROM");
	    	mailFromName = (String)requestData.getValue("MAILFROMNAME");
		mailTo = requestData.getValues("MAILTO");
		mailCc = requestData.getValues("MAILCC");
		mailBcc = requestData.getValues("MAILBCC");
		mailContent = (String)requestData.getValue("MAILCONTENT");
			
		//Context.log("busSendmail.sendHTML(): trying with new transport 4...");

	    	// compose the message
	    	try {
	    			//mailMsg.setHeader("Content-Transfer-Encoding","base64");
	    			mailMsg.setHeader("Content-Transfer-Encoding","8bit");
	    			mailMsg.setHeader("Content-type","text/html; charset=" + mailMimeCharset);
				mailMsg.setSentDate(new Date());
				mailMsg.setSubject(MimeUtility.encodeText(mailSubject, mailMimeCharset, "Q"));
				if(mailFrom == null || mailFrom.length() == 0)
					mailMsg.setFrom();
				else
					if(mailFromName == null || mailFromName.length() == 0)
						mailMsg.setFrom(new javax.mail.internet.InternetAddress(mailFrom));
					else
						mailMsg.setFrom(new javax.mail.internet.InternetAddress(mailFrom, MimeUtility.encodeText(mailFromName,"iso-8859-1", "Q")));

				// set the receipients of type TO
				receipientAddresses = new StringBuffer();
				for(int i = 0; i < mailTo.size(); i++) {
					mailReceiver = (String)mailTo.get(i);
					receipientAddresses.append(" " + mailReceiver);
				}
				receipientAddresses.deleteCharAt(0);		// remove the first blank space
				mailMsg.setRecipients(javax.mail.Message.RecipientType.TO,	InternetAddress.parse(receipientAddresses.toString(), false));

				// set the receipients of type CC
				if(mailCc != null) {
					receipientAddresses.setLength(0);
					for(int i = 0; i < mailCc.size(); i++) {
						mailReceiver = (String)mailCc.get(i);
						receipientAddresses.append(" " + mailReceiver);
					}
					receipientAddresses.deleteCharAt(0);		// remove the first blank space
					mailMsg.setRecipients(javax.mail.Message.RecipientType.CC,	InternetAddress.parse(receipientAddresses.toString(), false));
				}

				// set the receipients of type BCC
				if(mailBcc != null) {
					receipientAddresses.setLength(0);
					for(int i = 0; i < mailBcc.size(); i++) {
						mailReceiver = (String)mailCc.get(i);
						receipientAddresses.append(" " + mailReceiver);
					}
					receipientAddresses.deleteCharAt(0);		// remove the first blank space
					mailMsg.setRecipients(javax.mail.Message.RecipientType.BCC,	InternetAddress.parse(receipientAddresses.toString(), false));
				}

				//ds = new DataHandler(MimeUtility.encodeText(mailContent), "text/html");
				ds = new DataHandler(mailContent, "text/html;charset=" + mailMimeCharset);
				mailMsg.setDataHandler(ds);
			}
			catch(javax.mail.internet.AddressException e) {
				throw new busException("busSendMail.sendHTML() while composing the message threw exception javax.mail.internet.AddressException: " + e.getMessage(), e);
			}
			catch(javax.mail.IllegalWriteException e) {
				throw new busException("busSendMail.sendHTML() while composing the message threw exception javax.mail.IllegalWriteException: " + e.getMessage(), e);
			}
			catch(javax.mail.MessagingException e) {
				throw new busException("busSendMail.sendHTML() while composing the message threw exception javax.mail.MessagingException: " + e.getMessage(), e);
			}
			catch (UnsupportedEncodingException e) {
				throw new busException("busSendMail.sendHTML() while composing the message threw exception java.io.UnsupportedEncodingException: " + e.getMessage(), e);
			}

			// send the message
			try {
				javax.mail.Transport.send(mailMsg);
			}
			catch(javax.mail.SendFailedException e) {
				javax.mail.Address addresses[] = null;
				int len, i;

					requestData.addValue("MAILMSG", e.getMessage());
					addresses = e.getValidSentAddresses();
					if(addresses != null) {
						len = addresses.length;
						i=0;
						while(i < len) {
							requestData.addValue("MAILVALIDSENTADDRESSES", addresses[i].toString());
							i++;
						}
					}
					addresses = e.getValidUnsentAddresses();
					if(addresses != null) {
						len = addresses.length;
						i=0;
						while(i < len) {
							requestData.addValue("MAILVALIDUNSENTADDRESSES", addresses[i].toString());
							i++;
						}
					}
					addresses = e.getInvalidAddresses();
					if(addresses != null) {
						len = addresses.length;
						i=0;
						while(i < len) {
							requestData.addValue("MAILINVALIDADDRESSES", addresses[i].toString());
							i++;
						}
					}
					result = false;
			}
			catch(javax.mail.MessagingException e) {
				throw new busException("busSendMail.sendHTML() while sending the message threw exception javax.mail.MessagingException: " + e.getMessage(), e);
			}

			return result;

	}
}
