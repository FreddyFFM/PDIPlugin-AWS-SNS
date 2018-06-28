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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sns.AmazonSNS;

import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;


/**
 * @author Michael Fraedrich - https://github.com/FreddyFFM/PDIPlugin-AWS-SNS
 *
 */
public class SNSNotifyStepDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = SNSNotifyStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private SNSNotifyStepMeta meta;

	// text field holding the name of the field to add to the row stream
	private CTabFolder tabFolder;
	private CTabItem tbtmSettings;
	private ScrolledComposite scrlSettingsComp;
	private Composite settingsComp;
	private Label lblAWSKey;
	private TextVar tAWSKey;
	private Label lblAWSKeySecret;
	private PasswordTextVar tAWSKeySecret;
	private Label lblAWSRegion;
	private ComboVar tAWSRegion;
	private CTabItem tbtmNotifications;
	private ScrolledComposite scrlNotificationsComp;
	private Composite notificationsComp;
	private Label lblnotifyPoint;
	private Combo tnotifyPoint;
	private Label lblMessageID;
	private TextVar tMessageID;
	private ColumnInfo fieldColumn;
	private TableView tTableNotifyProps;

	private Label lblDevInfo;

	private Label lblAWSCredChain;

	private ComboVar tAWSCredChain;

	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public SNSNotifyStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (SNSNotifyStepMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SNSNotify.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		// ------------------------------------------------------- //
		// DEVELOPER INFO //
		// ------------------------------------------------------- //
		
		lblDevInfo = new Label(shell, SWT.RIGHT);
		props.setLook(lblDevInfo);
		FormData fdDevInfo = new FormData();
		fdDevInfo.bottom = new FormAttachment(100, -margin);
		fdDevInfo.right = new FormAttachment(100, -margin);

		lblDevInfo.setLayoutData(fdDevInfo);
		lblDevInfo.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Developer.PopUp.Title"));
		lblDevInfo.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Developer.PopUp.Title"));
				dialog.setMessage(BaseMessages.getString(PKG, "SNSNotifyStep.Developer.PopUp.Label"));

				// open dialog and await user selection
				dialog.open();
			}

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {

			}

			@Override
			public void mouseDown(MouseEvent arg0) {

			}

		});
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// ------------------------------------------------------- //
		// TABULATOREN START //
		// ------------------------------------------------------- //

		// TABS - ANFANG
		tabFolder = new CTabFolder(shell, SWT.BORDER);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.right = new FormAttachment(100, 0);
		fd_tabFolder.top = new FormAttachment(wStepname, margin);
		fd_tabFolder.left = new FormAttachment(0, 0);
		fd_tabFolder.bottom = new FormAttachment(100, -50);
		tabFolder.setLayoutData(fd_tabFolder);
		props.setLook(tabFolder);

		// ------------------------------------------------------- //
		// - TAB Settings START //
		// ------------------------------------------------------- //

		// Settings-TAB - ANFANG
		tbtmSettings = new CTabItem(tabFolder, SWT.NONE);
		tbtmSettings.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.Title"));

		scrlSettingsComp = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		scrlSettingsComp.setLayout(new FillLayout());
		props.setLook(scrlSettingsComp);

		settingsComp = new Composite(scrlSettingsComp, SWT.NONE);
		props.setLook(settingsComp);

		FormLayout settingsLayout = new FormLayout();
		settingsLayout.marginWidth = 3;
		settingsLayout.marginHeight = 3;
		settingsComp.setLayout(settingsLayout);
		
		// Use AWS Credentials Provider Chain
		// Credentials Chain
		lblAWSCredChain = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSCredChain);
		FormData fd_lblAWSCredChain = new FormData();
		fd_lblAWSCredChain.left = new FormAttachment(0, 0);
		fd_lblAWSCredChain.top = new FormAttachment(0, margin);
		fd_lblAWSCredChain.right = new FormAttachment(middle, -margin);
		lblAWSCredChain.setLayoutData(fd_lblAWSCredChain);
		lblAWSCredChain.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSCredChain.Label"));
		
		tAWSCredChain = new ComboVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tAWSCredChain);
		FormData fd_tAWSCredChain = new FormData();
		fd_tAWSCredChain.top = new FormAttachment(0, margin);
		fd_tAWSCredChain.left = new FormAttachment(middle, 0);
		fd_tAWSCredChain.right = new FormAttachment(100, 0);
		tAWSCredChain.setLayoutData(fd_tAWSCredChain);
		tAWSCredChain.setToolTipText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSCredChain.Tooltip"));		
		tAWSCredChain.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				changeCredentialChainSelection();				
			}			
		});
		
		// AWS Key
		lblAWSKey = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSKey);
		FormData fd_lblAWSKey = new FormData();
		fd_lblAWSKey.left = new FormAttachment(0, 0);
		fd_lblAWSKey.top = new FormAttachment(tAWSCredChain, margin);
		fd_lblAWSKey.right = new FormAttachment(middle, -margin);
		lblAWSKey.setLayoutData(fd_lblAWSKey);
		lblAWSKey.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSKey.Label"));

		tAWSKey = new TextVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tAWSKey);
		FormData fd_tAWSKey = new FormData();
		fd_tAWSKey.top = new FormAttachment(tAWSCredChain, margin);
		fd_tAWSKey.left = new FormAttachment(middle, 0);
		fd_tAWSKey.right = new FormAttachment(100, 0);
		tAWSKey.setLayoutData(fd_tAWSKey);
		tAWSKey.setToolTipText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSKey.Tooltip"));

		// AWS Key Secret
		lblAWSKeySecret = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSKeySecret);
		FormData fd_lblAWSKeySecret = new FormData();
		fd_lblAWSKeySecret.left = new FormAttachment(0, 0);
		fd_lblAWSKeySecret.top = new FormAttachment(tAWSKey, margin);
		fd_lblAWSKeySecret.right = new FormAttachment(middle, -margin);
		lblAWSKeySecret.setLayoutData(fd_lblAWSKeySecret);
		lblAWSKeySecret.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSKeySecret.Label"));

		tAWSKeySecret = new PasswordTextVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tAWSKeySecret);
		FormData fd_tAWSKeySecret = new FormData();
		fd_tAWSKeySecret.top = new FormAttachment(tAWSKey, margin);
		fd_tAWSKeySecret.left = new FormAttachment(middle, 0);
		fd_tAWSKeySecret.right = new FormAttachment(100, 0);
		tAWSKeySecret.setLayoutData(fd_tAWSKeySecret);
		tAWSKeySecret.setToolTipText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSKeySecret.Tooltip"));
		
		// AWS Region
		lblAWSRegion = new Label(settingsComp, SWT.RIGHT);
		props.setLook(lblAWSRegion);
		FormData fd_lblAWSRegion = new FormData();
		fd_lblAWSRegion.left = new FormAttachment(0, 0);
		fd_lblAWSRegion.top = new FormAttachment(tAWSKeySecret, margin);
		fd_lblAWSRegion.right = new FormAttachment(middle, -margin);
		lblAWSRegion.setLayoutData(fd_lblAWSRegion);
		lblAWSRegion.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSRegion.Label"));

		tAWSRegion = new ComboVar(transMeta, settingsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook(tAWSRegion);
		FormData fd_tAWSRegion = new FormData();
		fd_tAWSRegion.top = new FormAttachment(tAWSKeySecret, margin);
		fd_tAWSRegion.left = new FormAttachment(middle, 0);
		fd_tAWSRegion.right = new FormAttachment(100, 0);
		tAWSRegion.setLayoutData(fd_tAWSRegion);
		tAWSRegion
				.setToolTipText(BaseMessages.getString(PKG, "SNSNotifyStep.Settings.AWSRegion.Tooltip"));
		populateAWSRegion(tAWSRegion);
				
		Control[] queueTabList = new Control[] { tAWSKey, tAWSKeySecret, tAWSRegion };
		settingsComp.setTabList(queueTabList);

		settingsComp.pack();
		Rectangle settingsBounds = settingsComp.getBounds();

		scrlSettingsComp.setContent(settingsComp);
		scrlSettingsComp.setExpandHorizontal(true);
		scrlSettingsComp.setExpandVertical(true);
		scrlSettingsComp.setMinWidth(settingsBounds.width);
		scrlSettingsComp.setMinHeight(settingsBounds.height);
		// Settings-TAB - ENDE
		
		
		// ------------------------------------------------------- //
		// - TAB Notifications START //
		// ------------------------------------------------------- //

		// Notifications-TAB - ANFANG
		tbtmNotifications = new CTabItem(tabFolder, SWT.NONE);
		tbtmNotifications.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.Title"));

		scrlNotificationsComp = new ScrolledComposite(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		scrlNotificationsComp.setLayout(new FillLayout());
		props.setLook(scrlNotificationsComp);

		notificationsComp = new Composite(scrlNotificationsComp, SWT.NONE);
		props.setLook(notificationsComp);

		FormLayout notificationsLayout = new FormLayout();
		notificationsLayout.marginWidth = 3;
		notificationsLayout.marginHeight = 3;
		notificationsComp.setLayout(notificationsLayout);
		
		// FELDER
		// Notification Point
		lblnotifyPoint = new Label(notificationsComp, SWT.RIGHT);
		props.setLook(lblnotifyPoint);
		FormData fd_lblnotifyPoint = new FormData();
		fd_lblnotifyPoint.left = new FormAttachment(0, 0);
		fd_lblnotifyPoint.top = new FormAttachment(0, margin);
		fd_lblnotifyPoint.right = new FormAttachment(middle, -margin);
		lblnotifyPoint.setLayoutData(fd_lblnotifyPoint);
		lblnotifyPoint.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.notifyPoint.Label"));

		tnotifyPoint = new Combo(notificationsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		props.setLook(tnotifyPoint);
		FormData fd_tnotifyPoint = new FormData();
		fd_tnotifyPoint.top = new FormAttachment(0, margin);
		fd_tnotifyPoint.left = new FormAttachment(middle, 0);
		fd_tnotifyPoint.right = new FormAttachment(100, 0);
		tnotifyPoint.setLayoutData(fd_tnotifyPoint);
		tnotifyPoint
				.setToolTipText(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.notifyPoint.Tooltip"));
		tnotifyPoint.setItems(meta.getNotifyPointValues());
		
		// MessageID
		lblMessageID = new Label(notificationsComp, SWT.RIGHT);
		props.setLook(lblMessageID);
		FormData fd_lblMessageID = new FormData();
		fd_lblMessageID.left = new FormAttachment(0, 0);
		fd_lblMessageID.top = new FormAttachment(tnotifyPoint, margin);
		fd_lblMessageID.right = new FormAttachment(middle, -margin);
		lblMessageID.setLayoutData(fd_lblMessageID);
		lblMessageID.setText(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.MessageID.Label"));

		tMessageID = new TextVar(transMeta, notificationsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(tMessageID);
		FormData fd_tMessageID = new FormData();
		fd_tMessageID.top = new FormAttachment(tnotifyPoint, margin);
		fd_tMessageID.left = new FormAttachment(middle, 0);
		fd_tMessageID.right = new FormAttachment(100, 0);
		tMessageID.setLayoutData(fd_tMessageID);
		tMessageID
				.setToolTipText(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.MessageID.Tooltip"));
		
		// Notification-Value-Settings-Table
		// Properties Table
		int keyWidgetCols = 4;
		int keyWidgetRows = 3;

		// Create columns
		ColumnInfo[] ciNotifyProps = new ColumnInfo[keyWidgetCols];
		ciNotifyProps[0] = new ColumnInfo(
				BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.Property.Label"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);
		ciNotifyProps[1] = new ColumnInfo(
				BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.InField.Label"),
				ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y", "N"}, false);
		ciNotifyProps[2] = new ColumnInfo(
				BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.Field.Label"),
				ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false);
		ciNotifyProps[3] = new ColumnInfo(
				BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.Value.Label"),
				ColumnInfo.COLUMN_TYPE_TEXT, false);

		ciNotifyProps[0].setToolTip(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.Property.Tooltip"));
		ciNotifyProps[0].setReadOnly(true);
		ciNotifyProps[1].setToolTip(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.InField.Tooltip"));
		ciNotifyProps[2].setToolTip(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.Field.Tooltip"));		
		ciNotifyProps[3].setToolTip(BaseMessages.getString(PKG, "SNSNotifyStep.Notifications.ValueDef.Value.Tooltip"));
		ciNotifyProps[3].setUsingVariables(true);

		fieldColumn = ciNotifyProps[2];

		// Create Table
		tTableNotifyProps = new TableView(transMeta, notificationsComp,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciNotifyProps,
				keyWidgetRows, lsMod, props);

		FormData fd_TableNotifyProps = new FormData();
		fd_TableNotifyProps.left = new FormAttachment(0, 0);
		fd_TableNotifyProps.top = new FormAttachment(tMessageID, margin);
		fd_TableNotifyProps.right = new FormAttachment(100, 0);
		fd_TableNotifyProps.bottom = new FormAttachment(100, -margin);
		tTableNotifyProps.setLayoutData(fd_TableNotifyProps);
		
		Control[] notificationTabList = new Control[] { tnotifyPoint, tMessageID, tTableNotifyProps };
		notificationsComp.setTabList(notificationTabList);

		notificationsComp.pack();
		Rectangle NotificationsBounds = notificationsComp.getBounds();

		scrlNotificationsComp.setContent(notificationsComp);
		scrlNotificationsComp.setExpandHorizontal(true);
		scrlNotificationsComp.setExpandVertical(true);
		scrlNotificationsComp.setMinWidth(NotificationsBounds.width);
		scrlNotificationsComp.setMinHeight(NotificationsBounds.height);
		// Notifications-TAB - Ende
		
		scrlSettingsComp.layout();
		tbtmSettings.setControl(scrlSettingsComp);

		scrlNotificationsComp.layout();
		tbtmNotifications.setControl(scrlNotificationsComp);

		tabFolder.setSelection(0);

		// TABS ENDE
		      
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		tAWSKey.addSelectionListener(lsDef);
		tAWSKeySecret.addSelectionListener(lsDef);
		tAWSRegion.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateYesNoSelection();
		populateDialog();		
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	protected void changeCredentialChainSelection() {
		// Output-Info set in fields
		if (tAWSCredChain.getText().equalsIgnoreCase("Y")) {

			// Settings-Fields
			lblAWSKey.setEnabled(false);
			tAWSKey.setEnabled(false);
			lblAWSKeySecret.setEnabled(false);
			tAWSKeySecret.setEnabled(false);
			lblAWSRegion.setEnabled(false);
			tAWSRegion.setEnabled(false);

			// Output-Info set in Config
		} else {

			// Settings-Fields
			lblAWSKey.setEnabled(true);
			tAWSKey.setEnabled(true);
			lblAWSKeySecret.setEnabled(true);
			tAWSKeySecret.setEnabled(true);
			lblAWSRegion.setEnabled(true);
			tAWSRegion.setEnabled(true);

		}

		meta.setChanged();
		
	}
	
	private void populateYesNoSelection() {
		
		tAWSCredChain.removeAll();				
		tAWSCredChain.add("Y");
		tAWSCredChain.add("N");
		tAWSCredChain.select(0);

	}	

	/**
	 * This methods set the Input-Fields in Column Field for each table-row
	 */
	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {

				RowMetaInterface prevFields;

				try {
					prevFields = transMeta.getPrevStepFields(stepname);
				} catch (KettleException e) {
					prevFields = new RowMeta();
					logError(BaseMessages.getString(PKG, "SNSNotifyStep.ErrorText.NoPrevFields"));
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				Arrays.sort(prevStepFieldNames);
				fieldColumn.setComboValues(prevStepFieldNames);
			}
		};
		new Thread(fieldLoader).start();
	}

	/**
	 * This method fills the CombarVar with all available AWS Regions
	 * 
	 * @param ComboVar tAWSRegion2
	 */
	private void populateAWSRegion(ComboVar tAWSRegion2) {
		
		tAWSRegion2.removeAll();
			
		try {
		
			List<Region> snsRegions = RegionUtils.getRegionsForService(AmazonSNS.ENDPOINT_PREFIX);
			
			for (Iterator<Region> i = snsRegions.iterator(); i.hasNext();) {
				Region region = i.next();
				tAWSRegion2.add(region.getName());
			}

		} catch (AmazonClientException e) {
			logError(BaseMessages.getString(PKG, e.getMessage()));
		}
		
	}

	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		
		tAWSCredChain.setText(meta.getAWSCredChain());
		tAWSKey.setText(meta.getAWSKey());	
		tAWSKeySecret.setText(meta.getAWSKeySecret());
		tAWSRegion.setText(meta.getAWSRegion());
		tnotifyPoint.setText(meta.getNotifyPoint());
		tMessageID.setText(meta.gettFldMessageID());
		
		// Populate NotifyProperties	
		setComboValues();
		
		tTableNotifyProps.setText("topicARN", 1, 0);
		tTableNotifyProps.setText(meta.getcInputtopicArn(), 2, 0);
		tTableNotifyProps.setText(meta.gettFldtopicARN(), 3, 0);
		tTableNotifyProps.setText(meta.gettValuetopicARN(), 4, 0);
		
		tTableNotifyProps.setText("Subject", 1, 1);
		tTableNotifyProps.setText(meta.getcInputSubject(), 2, 1);
		tTableNotifyProps.setText(meta.gettFldSubject(), 3, 1);
		tTableNotifyProps.setText(meta.gettValueSubject(), 4, 1);
		
		tTableNotifyProps.setText("Message", 1, 2);
		tTableNotifyProps.setText(meta.getcInputMessage(), 2, 2);
		tTableNotifyProps.setText(meta.gettFldMessage(), 3, 2);
		tTableNotifyProps.setText(meta.gettValueMessage(), 4, 2);
		
	}

	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to step name from the dialog control
		stepname = wStepname.getText(); 
		
		// Setting the  settings to the meta object
		meta.setAWSCredChain(tAWSCredChain.getText());
		meta.setAWSKey(tAWSKey.getText());
		meta.setAWSKeySecret(tAWSKeySecret.getText());
		meta.setAWSRegion(tAWSRegion.getText());
		meta.setNotifyPoint(tnotifyPoint.getText());
		meta.settFldMessageID(tMessageID.getText());
		
		int nrKeys = tTableNotifyProps.nrNonEmpty();

		for (int i = 0; i < nrKeys; i++) {
			TableItem item = tTableNotifyProps.getNonEmpty(i);
			if (item.getText(1).equals("topicARN")) {
				meta.setcInputtopicArn(item.getText(2).isEmpty() ? "N" : item.getText(2));
				meta.settFldtopicARN(item.getText(3));
				meta.settValuetopicARN(item.getText(4));
			}
			if (item.getText(1).equals("Subject")) {
				meta.setcInputSubject(item.getText(2).isEmpty() ? "N" : item.getText(2));
				meta.settFldSubject(item.getText(3));
				meta.settValueSubject(item.getText(4));
			}
			if (item.getText(1).equals("Message")) {
				meta.setcInputMessage(item.getText(2).isEmpty() ? "N" : item.getText(2));
				meta.settFldMessage(item.getText(3));
				meta.settValueMessage(item.getText(4));
			}
		}
		
		// close the SWT dialog window
		dispose();
	}
}
