package de.freddy4u.pentaho.di.steps.aws_sns_notify.aws_sns;

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import de.freddy4u.pentaho.di.steps.aws_sns_notify.SNSNotifyStepMeta;


/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
public class AWS_SNS {
	
	private AmazonSNSClient snsClient;	
	private SNSNotifyStepMeta meta;
	private String awsKey;
	private String awsRegion;
	private String awsKeySecret;
	private BaseStep baseStep;
	private TransMeta transMeta;
	
	/**
	 * 
	 * Constructor for new AWS SNS Object
	 * 
	 * @param smi	StepMetaInterface
	 * @param t		TransMeta
	 * @param bst	BaseStep
	 */
	public AWS_SNS(StepMetaInterface smi, TransMeta t, BaseStep bst) {
		
		this.meta = (SNSNotifyStepMeta) smi;
		this.baseStep = (BaseStep) bst;
		this.transMeta = t;
		
		this.awsKey = transMeta.environmentSubstitute(meta.getAWSKey());
		this.awsKeySecret = transMeta.environmentSubstitute(meta.getAWSKeySecret());
		this.awsRegion = transMeta.environmentSubstitute(meta.getAWSRegion());
	}
	
	/**
	 * 
	 * Establishing new Connection to Amazon Webservices
	 * 
	 * @return	true on successfull connection
	 */
	public boolean getAWSConnection() {
		try {
			baseStep.logBasic("Starting connection to AWS SNS");
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(this.awsKey, this.awsKeySecret);
			snsClient = (AmazonSNSClient) AmazonSNSClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withRegion(transMeta.environmentSubstitute(this.awsRegion))
					.build();
			
			baseStep.logBasic("Connected to SNS in Region " + this.awsRegion + " with API-Key >>" + this.awsKey + "<<");
			
			return true;
		} catch (AmazonClientException e) {			
			baseStep.logError(e.getMessage());
		}		
		
		return false;
	}
	
	
	/**
	 * Disconnects from AWS
	 */
	public void disconnectAWSConnection() {
		try {
			snsClient.shutdown();
			
			baseStep.logBasic("Disconnected from SNS in Region " + this.awsRegion);
	
		} catch (AmazonClientException e) {
			baseStep.logError(e.getMessage());
		}
		
	}
	
	/**
	 * 
	 * Publish Message with Subject to a topicARN
	 * 
	 * @param tARN	AWS ARN for SNS topic
	 * @param subj	Subject for Message
	 * @param msg	Message Content
	 * @return		SNS messageID on successfull publish
	 */
	public String publishToSNS(String tARN, String subj, String msg) {
		
		String topicARN = transMeta.environmentSubstitute(tARN);
		String subject = transMeta.environmentSubstitute(subj);
		String message = transMeta.environmentSubstitute(msg);
		
		try {
				
			PublishRequest publishRequest = new PublishRequest(topicARN, message, subject);
			PublishResult publishResult = snsClient.publish(publishRequest);
			String messageId = publishResult.getMessageId();
			baseStep.logBasic(messageId);
			return messageId;
			
		} catch (AmazonSNSException e) {
			baseStep.logError(e.getErrorMessage());
		}
		
		return null;
	}

}
