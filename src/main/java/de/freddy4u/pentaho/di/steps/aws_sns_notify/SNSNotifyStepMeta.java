/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package de.freddy4u.pentaho.di.steps.aws_sns_notify;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;



/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
@Step(	
		id = "AwsSNSnotify",
		image = "de/freddy4u/pentaho/di/steps/aws_sns_notify/resources/aws_sns_notify.svg",
		i18nPackageName="de.freddy4u.pentaho.di.steps.aws_sns_notify",
		name="SNSNotifyStep.Name",
		description = "SNSNotifyStep.TooltipDesc",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
		documentationUrl="https://freddyffm.github.io/PDIPlugin-AWS-SNS/"
)
public class SNSNotifyStepMeta extends BaseStepMeta implements StepMetaInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = SNSNotifyStepMeta.class; // for i18n purposes
	
	private String awsKey;
	private String awsKeySecret;
	private String awsRegion;
	private String notifyPoint;
	private String cInputtopicArn;
	private String tFldtopicARN;
	private String tValuetopicARN;
	private String cInputSubject;
	private String tFldSubject;
	private String tValueSubject;
	private String cInputMessage;
	private String tFldMessage;
	private String tValueMessage;
	private String tFldMessageID;

	/**
	 * Constructor should call super() to make sure the base class has a chance to initialize properly.
	 */
	public SNSNotifyStepMeta() {
		super(); 
	}
	
	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step.
	 * A standard implementation passing the arguments to the constructor of the step dialog is recommended.
	 * 
	 * @param shell		an SWT Shell
	 * @param meta 		description of the step 
	 * @param transMeta	description of the the transformation 
	 * @param name		the name of the step
	 * @return 			new instance of a dialog for this step 
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new SNSNotifyStepDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. 
	 * A standard implementation passing the arguments to the constructor of the step class is recommended.
	 * 
	 * @param stepMeta				description of the step
	 * @param stepDataInterface		instance of a step data class
	 * @param cnr					copy number
	 * @param transMeta				description of the transformation
	 * @param disp					runtime implementation of the transformation
	 * @return						the new instance of a step implementation 
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new SNSNotifyStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new SNSNotifyStepData();
	}	

	/**
	 * This method is called every time a new step is created and should allocate/set the step configuration
	 * to sensible defaults. The values set here will be used by Spoon when a new step is created.    
	 */
	public void setDefault() {
		// outputField = "demo_field";
	}
	
	public String getAWSKey() {
		return awsKey == null ? "" : awsKey;
	}

	public void setAWSKey(String aws_key) {
		this.awsKey = aws_key;
	}
	
	public String getAWSKeySecret() {
		return awsKeySecret == null ? "" : awsKeySecret;
	}

	public void setAWSKeySecret(String awsKeySecret) {
		this.awsKeySecret = awsKeySecret;
	}

	public String getAWSRegion() {
		return awsRegion == null ? "" : awsRegion;
	}

	public void setAWSRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}

	public String getNotifyPoint() {
		return notifyPoint == null ? "Send once with first row" : notifyPoint;
	}
	
	public String[] getNotifyPointValues() {
		
		String[] values = new String[] {
			"Send once with first row",
			//"Send once with last row",
			"Send for each row (be careful!)"
		};
		
		return values;	
	}
	
	public String getNotifyPointShort() {
		if (notifyPoint.contains("last")) {
			return "last";
		} else if (notifyPoint.contains("each")) {
			return "each";			
		} else {
			return "first";
		}
	}

	public void setNotifyPoint(String notifyPoint) {
		this.notifyPoint = notifyPoint;
	}

	public String getcInputtopicArn() {
		return cInputtopicArn == null ? "N" : cInputtopicArn;
	}

	public void setcInputtopicArn(String cInputtopicArn) {
		this.cInputtopicArn = cInputtopicArn;
	}

	public String gettFldtopicARN() {
		return tFldtopicARN == null ? "" : tFldtopicARN;
	}

	public void settFldtopicARN(String tFldtopicARN) {
		this.tFldtopicARN = tFldtopicARN;
	}

	public String gettValuetopicARN() {
		return tValuetopicARN == null ? "" : tValuetopicARN;
	}

	public void settValuetopicARN(String tValuetopicARN) {
		this.tValuetopicARN = tValuetopicARN;
	}

	public String getcInputSubject() {
		return cInputSubject == null ? "N" : cInputSubject;
	}

	public void setcInputSubject(String cInputSubject) {
		this.cInputSubject = cInputSubject;
	}

	public String gettFldSubject() {
		return tFldSubject == null ? "" : tFldSubject;
	}

	public void settFldSubject(String tFldSubject) {
		this.tFldSubject = tFldSubject;
	}

	public String gettValueSubject() {
		return tValueSubject == null ? "" : tValueSubject;
	}

	public void settValueSubject(String tValueSubject) {
		this.tValueSubject = tValueSubject;
	}

	public String getcInputMessage() {
		return cInputMessage == null ? "N" : cInputMessage;
	}

	public void setcInputMessage(String cInputMessage) {
		this.cInputMessage = cInputMessage;
	}

	public String gettFldMessage() {
		return tFldMessage == null ? "N" : tFldMessage;
	}

	public void settFldMessage(String tFldMessage) {
		this.tFldMessage = tFldMessage;
	}

	public String gettValueMessage() {
		return tValueMessage == null ? "" : tValueMessage;
	}

	public void settValueMessage(String tValueMessage) {
		this.tValueMessage = tValueMessage;
	}

	public String gettFldMessageID() {
		return tFldMessageID == null ? "" : tFldMessageID;
	}

	public void settFldMessageID(String tFldMessageID) {
		this.tFldMessageID = tFldMessageID;
	}

	/**
	 * This method is used when a step is duplicated in Spoon. It needs to return a deep copy of this
	 * step meta object. Be sure to create proper deep copies if the step configuration is stored in
	 * modifiable objects.
	 * 
	 * See org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta.clone() for an example on creating
	 * a deep copy.
	 * 
	 * @return a deep copy of this
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}
	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
	 * return value is an XML fragment consisting of one or more XML tags.  
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
	 * 
	 * @return a string containing the XML serialization of this step
	 */
	public String getXML() throws KettleValueException {
		
		// only one field to serialize
		String xml = "";
		xml += XMLHandler.addTagValue("AWSKey", awsKey);
		xml += XMLHandler.addTagValue("AWSKeySecret", awsKeySecret);
		xml += XMLHandler.addTagValue("AWSRegion", awsRegion);
		xml += XMLHandler.addTagValue("NotifyPoint", notifyPoint);
		xml += XMLHandler.addTagValue("ChooserTopicARN", cInputtopicArn);
		xml += XMLHandler.addTagValue("ChooserSubject", cInputSubject);
		xml += XMLHandler.addTagValue("ChooserMessage", cInputMessage);
		xml += XMLHandler.addTagValue("FieldtopicARN", tFldtopicARN);
		xml += XMLHandler.addTagValue("FieldSubject", tFldSubject);
		xml += XMLHandler.addTagValue("FieldMessage", tFldMessage);
		xml += XMLHandler.addTagValue("ValuetopicARN", tValuetopicARN);
		xml += XMLHandler.addTagValue("ValueSubject", tValueSubject);
		xml += XMLHandler.addTagValue("ValueMessage", tValueMessage);
		xml += XMLHandler.addTagValue("FieldMessageID", tFldMessageID);
		return xml;
	}

	/**
	 * This method is called by PDI when a step needs to load its configuration from XML.
	 * 
	 * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
	 * XML node passed in.
	 * 
	 * @param stepnode	the XML node containing the configuration
	 * @param databases	the databases available in the transformation
	 * @param metaStore the metaStore to optionally read from
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {

		try {
			setAWSKey(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSKey")));
			setAWSKeySecret(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSKeySecret")));
			setAWSRegion(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "AWSRegion")));
			setNotifyPoint(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "NotifyPoint")));
			setcInputtopicArn(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "ChooserTopicARN")));
			setcInputSubject(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "ChooserSubject")));
			setcInputMessage(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "ChooserMessage")));
			settFldtopicARN(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldtopicARN")));
			settFldSubject(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldSubject")));
			settFldMessage(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldMessage")));
			settValuetopicARN(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "ValuetopicARN")));
			settValueSubject(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "ValueSubject")));
			settValueMessage(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "ValueMessage")));
			settFldMessageID(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "FieldMessageID")));
		} catch (Exception e) {
			throw new KettleXMLException("Demo plugin unable to read step info from XML node", e);
		}

	}	
	/**
	 * This method is called by Spoon when a step needs to serialize its configuration to a repository.
	 * The repository implementation provides the necessary methods to save the step attributes.
	 *
	 * @param rep					the repository to save to
	 * @param metaStore				the metaStore to optionally write to
	 * @param id_transformation		the id to use for the transformation when saving
	 * @param id_step				the id to use for the step  when saving
	 */
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		
		try{
			rep.saveStepAttribute(id_transformation, id_step, "AWSKey", awsKey); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "AWSKeySecret", awsKeySecret); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "AWSRegion", awsRegion); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "NotifyPoint", notifyPoint); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "ChooserTopicARN", cInputtopicArn); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "ChooserSubject", cInputSubject); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "ChooserMessage", cInputMessage); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldtopicARN", tFldtopicARN); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldSubject", tFldSubject); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldMessage", tFldMessage); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "ValuetopicARN", tValuetopicARN); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "ValueSubject", tValueSubject); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "ValueMessage", tValueMessage); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "FieldMessageID", tFldMessageID); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to save step into repository: "+id_step, e); 
		}
	}		
	
	/**
	 * This method is called by PDI when a step needs to read its configuration from a repository.
	 * The repository implementation provides the necessary methods to read the step attributes.
	 * 
	 * @param rep		the repository to read from
	 * @param metaStore	the metaStore to optionally read from
	 * @param id_step	the id of the step being read
	 * @param databases	the databases available in the transformation
	 * @param counters	the counters available in the transformation
	 */
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException  {
		
		try{
			awsKey  = rep.getStepAttributeString(id_step, "AWSKey"); //$NON-NLS-1$
			awsKeySecret  = rep.getStepAttributeString(id_step, "AWSKeySecret"); //$NON-NLS-1$
			awsRegion  = rep.getStepAttributeString(id_step, "AWSRegion"); //$NON-NLS-1$
			notifyPoint  = rep.getStepAttributeString(id_step, "NotifyPoint"); //$NON-NLS-1$
			cInputtopicArn  = rep.getStepAttributeString(id_step, "ChooserTopicARN"); //$NON-NLS-1$
			cInputSubject  = rep.getStepAttributeString(id_step, "ChooserSubject"); //$NON-NLS-1$
			cInputMessage  = rep.getStepAttributeString(id_step, "ChooserMessage"); //$NON-NLS-1$
			tFldtopicARN  = rep.getStepAttributeString(id_step, "FieldtopicARN"); //$NON-NLS-1$
			tFldSubject  = rep.getStepAttributeString(id_step, "FieldSubject"); //$NON-NLS-1$
			tFldMessage  = rep.getStepAttributeString(id_step, "FieldMessage"); //$NON-NLS-1$
			tValuetopicARN  = rep.getStepAttributeString(id_step, "ValuetopicARN"); //$NON-NLS-1$
			tValueSubject  = rep.getStepAttributeString(id_step, "ValueSubject"); //$NON-NLS-1$
			tValueMessage  = rep.getStepAttributeString(id_step, "ValueMessage"); //$NON-NLS-1$
			tFldMessageID  = rep.getStepAttributeString(id_step, "FieldMessageID"); //$NON-NLS-1$
		}
		catch(Exception e){
			throw new KettleException("Unable to load step from repository", e);
		}
	}

	/**
	 * This method is called to determine the changes the step is making to the row-stream.
	 * To that end a RowMetaInterface object is passed in, containing the row-stream structure as it is when entering
	 * the step. This method must apply any changes the step makes to the row stream. Usually a step adds fields to the
	 * row-stream.
	 * 
	 * @param inputRowMeta		the row structure coming in to the step
	 * @param name 				the name of the step making the changes
	 * @param info				row structures of any info steps coming in
	 * @param nextStep			the description of a step this step is passing rows to
	 * @param space				the variable space for resolving variables
	 * @param repository		the repository instance optionally read from
	 * @param metaStore			the metaStore to optionally read from
	 */
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException{

		/*
		 * This implementation appends the outputField to the row-stream
		 */
		
		try {
		
			if (tFldMessageID != null && !tFldMessageID.equals("")) {
				ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta(tFldMessageID, ValueMetaInterface.TYPE_STRING );
				valueMeta.setName(tFldMessageID.toUpperCase());
				valueMeta.setTrimType(ValueMetaInterface.TRIM_TYPE_BOTH);
				valueMeta.setOrigin(name);
				inputRowMeta.addValueMeta(valueMeta);
			}
			
		} catch (KettlePluginException e) {
			logBasic(e.getMessage());
			throw new KettleStepException( e );
		}
		
	}

	/**
	 * This method is called when the user selects the "Verify Transformation" option in Spoon. 
	 * A list of remarks is passed in that this method should add to. Each remark is a comment, warning, error, or ok.
	 * The method should perform as many checks as necessary to catch design-time errors.
	 * 
	 * Typical checks include:
	 * - verify that all mandatory configuration is given
	 * - verify that the step receives any input, unless it's a row generating step
	 * - verify that the step does not receive any input if it does not take them into account
	 * - verify that the step finds fields it relies on in the row-stream
	 * 
	 *   @param remarks		the list of remarks to append to
	 *   @param transmeta	the description of the transformation
	 *   @param stepMeta	the description of the step
	 *   @param prev		the structure of the incoming row-stream
	 *   @param input		names of steps sending input to the step
	 *   @param output		names of steps this step is sending output to
	 *   @param info		fields coming in from info steps 
	 *   @param metaStore	metaStore to optionally read from
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore)  {
		
		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.ReceivingRows.OK"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SNSNotify.CheckResult.ReceivingRows.ERROR"), stepMeta);
			remarks.add(cr);
		}	
		
		// Check for Credentials
		if (getAWSKey().isEmpty() || getAWSKeySecret().isEmpty() || getAWSRegion().isEmpty()) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SNSNotify.CheckResult.AWSCredentials.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.AWSCredentials.OK"), stepMeta);
			remarks.add(cr);	
		}	
		
		// Check for NotifyPoint
		if (getNotifyPoint().isEmpty()) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SNSNotify.CheckResult.NotifyPoint.ERROR"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.NotifyPoint.OK"), stepMeta);
			remarks.add(cr);
		}	
	
		// Check for NotifySettings
		boolean notifyPropsError = false;
		if ((getcInputtopicArn().equals("Y") && gettFldtopicARN().isEmpty()) || (getcInputtopicArn().equals("N") && gettValuetopicARN().isEmpty())) {
			notifyPropsError = true;
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.NotifyProps.topicARN.ERROR"), stepMeta);
			remarks.add(cr);
		}
		if ((getcInputSubject().equals("Y") && gettFldSubject().isEmpty()) || (getcInputSubject().equals("N") && gettValueSubject().isEmpty())) {
			notifyPropsError = true;
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.NotifyProps.Subject.ERROR"), stepMeta);
			remarks.add(cr);
		}
		if ((getcInputMessage().equals("Y") && gettFldMessage().isEmpty()) || (getcInputMessage().equals("N") && gettValueMessage().isEmpty())) {
			notifyPropsError = true;
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.NotifyProps.Message.ERROR"), stepMeta);
			remarks.add(cr);
		}
		
		if (!notifyPropsError) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SNSNotify.CheckResult.NotifyProps.OK"), stepMeta);
			remarks.add(cr);
		}
    	
	}


}
