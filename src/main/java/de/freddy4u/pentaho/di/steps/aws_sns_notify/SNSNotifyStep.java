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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import de.freddy4u.pentaho.di.steps.aws_sns_notify.aws_sns.AWS_SNS;

/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
public class SNSNotifyStep extends BaseStep implements StepInterface {

	private TransMeta transMeta;

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public SNSNotifyStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
		this.transMeta = t;
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It's establishing the AWS Connection
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		SNSNotifyStepMeta meta = (SNSNotifyStepMeta) smi;
		SNSNotifyStepData data = (SNSNotifyStepData) sdi;
		
		data.aws_sns = new AWS_SNS(smi, this.transMeta, this);
		if (!data.aws_sns.getAWSConnection()) {
			return false;
		}

		return super.init(meta, data);
	}	
	
	/**
	 * 
	 * This methods gets the Input-Fields indices (if defined) or set them to -1
	 * 
	 * @param data 	SNSNotifyStepData
	 * @param meta	SNSNotifyStepMeta
	 */
	private void setFieldIndices(SNSNotifyStepData data, SNSNotifyStepMeta meta) {
		// topicARN
		
		RowMetaInterface inputMeta = (RowMetaInterface) getInputRowMeta();
		
		if (meta.getcInputtopicArn().equals("Y")) {
			data.indexOfFieldtopARN = inputMeta.indexOfValue(meta.gettFldtopicARN());
		} else {
			data.indexOfFieldtopARN = -1;
		}
		
		// Subject
		if (meta.getcInputSubject().equals("Y")) {
			data.indexOfFieldSubject = inputMeta.indexOfValue(meta.gettFldSubject());
		} else {
			data.indexOfFieldSubject = -1;
		}
		
		// Message
		if (meta.getcInputMessage().equals("Y")) {
			data.indexOfFieldMessage = inputMeta.indexOfValue(meta.gettFldMessage());
		} else {
			data.indexOfFieldMessage = -1;
		}
		
	}

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		SNSNotifyStepMeta meta = (SNSNotifyStepMeta) smi;
		SNSNotifyStepData data = (SNSNotifyStepData) sdi;

		// get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
		Object[] r = getRow(); 
		
		// if no more rows are expected, indicate step is finished and processRow() should not be called again
		if (r == null){
			setOutputDone();
			return false;
		}
		
		// Get Field Indices of InputRow
		setFieldIndices(data, meta);

		// the "first" flag is inherited from the base step implementation
		// it is used to guard some processing tasks, like figuring out field indexes
		// in the row structure that only need to be done once
		boolean firstrow = false;
		
		if (first) {
			firstrow = true;
			first = false;
			// clone the input row structure and place it in our data object
			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			// use meta.getFields() to change it, so it reflects the output row structure 
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
		}
		
		// Check if it is first row or notification is set for each row
		if (meta.getNotifyPointShort().equals("each") || firstrow) {
			Object[] outputRow = sendSNSNotification(meta, data, r);
			if (outputRow != null) {
				putRow(data.outputRowMeta, outputRow);
				incrementLinesOutput();
			}
		} else {
			putRow(data.outputRowMeta, r);
		}
		
		// incrementLinesWritten
		incrementLinesWritten();

		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}		

		// indicate that processRow() should be called again
		return true;
	}
	
	/**
	 * 
	 * Prepare SNS Notification, send it and store MessageID
	 * 
	 * @param smi	SNSNotifyStepMeta
	 * @param sdi	SNSNotifyStepData
	 * @param row	Current processed row Object
	 * @return		Modified row Object or null on error
	 */
	private Object[] sendSNSNotification(SNSNotifyStepMeta smi, SNSNotifyStepData sdi, Object[] row) {
		
		SNSNotifyStepMeta meta = (SNSNotifyStepMeta) smi;
		SNSNotifyStepData data = (SNSNotifyStepData) sdi;
		
		try {
		
			// Notification Content from fields or static input
			String tARN = "";
			String subject = "";
			String message = "";
			
			// topicARN
			if (data.indexOfFieldtopARN >= 0) {
				tARN = getInputRowMeta().getString(row, data.indexOfFieldtopARN);
			} else {
				tARN = transMeta.environmentSubstitute(meta.gettValuetopicARN());
			}
			// Subject
			if (data.indexOfFieldSubject >= 0) {
				subject = getInputRowMeta().getString(row, data.indexOfFieldSubject);
			} else {
				subject = transMeta.environmentSubstitute(meta.gettValueSubject());
			}
			// Message
			if (data.indexOfFieldMessage >= 0) {
				message = getInputRowMeta().getString(row, data.indexOfFieldMessage);
			} else {
				message = transMeta.environmentSubstitute(meta.gettValueMessage());
			}
		
			// Send notification and catch messageID
			Object[] outputRowData = row;
			
			String messageID = data.aws_sns.publishToSNS(tARN, subject, message);
			
			if (messageID != null) {
						
				outputRowData = RowDataUtil.resizeArray(outputRowData, data.outputRowMeta.size());
		
				int indexOfMessID = data.outputRowMeta.indexOfValue(meta.gettFldMessageID());
				if (indexOfMessID >= 0) {
					outputRowData[indexOfMessID] = messageID;
				}
			}
			
			return outputRowData;
			
		} catch (Exception e) {
			logError(e.getMessage());
		}
		
		return null;
	}

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		SNSNotifyStepMeta meta = (SNSNotifyStepMeta) smi;
		SNSNotifyStepData data = (SNSNotifyStepData) sdi;
		
		data.aws_sns.disconnectAWSConnection();
		
		super.dispose(meta, data);
	}

}
