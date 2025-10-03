package com.example.appilot.automations.InstagramFollowerBot;

import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.appilot.automations.Interfaces.Action;
import com.example.appilot.automations.PopUpHandlers.Instagram.PopUpHandler;
import com.example.appilot.utils.HelperFunctions;

import java.util.Objects;
import java.util.Random;
public class Method7 {
    private static final String TAG = "method7";
    private final HelperFunctions helperFunctions;
    private final InstagramFollowerBotAutomation instagramFollowerBotAutomation;
    private final Handler handler;
    private final Random random;
    private final AccountManager accountManager;
    private final PopUpHandler popUpHandler;

    public Method7(InstagramFollowerBotAutomation instance, HelperFunctions helperFunctions, Handler handler, Random random, AccountManager manager, PopUpHandler popUpHandler) {
        this.helperFunctions = helperFunctions;
        this.instagramFollowerBotAutomation = instance;
        this.handler = handler;
        this.random = random;
        this.accountManager = manager;
        this.popUpHandler = popUpHandler;
    }

    public void switchPublicToPrivate() {
        Log.i(TAG, "Entered switchPublicToPrivate");
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        if (popUpHandler.handleOtherPopups(this::switchPublicToPrivate, null)) return;

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, inside AccesptAllRequests");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
            return;
        }

        AccessibilityNodeInfo OptionsButton = helperFunctions.findButtonByContentDesc(rootNode, "Options");

        if (OptionsButton != null) {
            Log.d(TAG, "Last button found in actionBar: " + OptionsButton.getText());
            if (OptionsButton.isClickable() && OptionsButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                Log.i(TAG, "Clicked on the Option button successfully");
                handler.postDelayed(() -> this.findPrivacySettingButton(10), 2000 + random.nextInt(500));
            } else {
                Log.e(TAG, "Option button is not clickable, going to click through gesture");
                this.instagramFollowerBotAutomation.getBoundsAndClick(OptionsButton, () -> this.findPrivacySettingButton(10), "Center", 2000, 2500);
            }
        } else {
            Log.e(TAG, "No button found in actionBar");
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
        }
    }

    private void findPrivacySettingButton(int Attempts) {
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        if (popUpHandler.handleOtherPopups(()->this.findPrivacySettingButton(Attempts), null)) return;

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, inside AccesptAllRequests");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
            return;
        }

        AccessibilityNodeInfo privacyButtonTextNode = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "Account privacy");
        if (privacyButtonTextNode == null) {
            if (Attempts > 0) {
                this.instagramFollowerBotAutomation.performStaticScrollUp(() -> this.findPrivacySettingButton(Attempts - 1));
                return;
            }
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            return;
        }

        AccessibilityNodeInfo buttonNode = privacyButtonTextNode.getParent().getParent();

        if (buttonNode == null) {
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            return;
        }

        if (buttonNode.isClickable() && buttonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            Log.i(TAG, "Entered Account Privacy Setting");
            handler.postDelayed(this::ConvertAccountPrivacy, 2000 + random.nextInt(1000));
        }

    }

    private void ConvertAccountPrivacy() {
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, inside AccesptAllRequests");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
            return;
        }


        AccessibilityNodeInfo privacyButtonTextNode = helperFunctions.findNodeByClassAndText(rootNode, "android.widget.TextView", "Private account");

        if (privacyButtonTextNode == null) {
            Log.e(TAG, "privacyButtonTextNode is null, inside AccesptAllRequests");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
            return;
        }
        Log.i(TAG, "Found privacyButtonTextNode");
        AccessibilityNodeInfo privacyToggleButton = privacyButtonTextNode.getParent().getParent();

        if (privacyToggleButton == null) {
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            return;
        }
        Log.i(TAG, "Found privacyToggleButton");

        if (privacyToggleButton.isClickable() && privacyToggleButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            int delayTime = 2000 + Math.max(1, random.nextInt(2000));
            handler.postDelayed(this::handleSwitchToPrivateConfirmation, delayTime);
        } else {
            this.instagramFollowerBotAutomation.getBoundsAndClick(privacyToggleButton, this::handleSwitchToPrivateConfirmation, "Last", 2000, 2000);
        }
    }

    private void handleSwitchToPrivateConfirmation(){
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) return;

        AccessibilityNodeInfo rootNode = helperFunctions.getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, inside handleConfirmationSlider");
            this.helperFunctions.cleanupAndExit("Automation Could not be Completed. Please make sure The Device has Accessibility enabled.", "error");
            return;
        }

        AccessibilityNodeInfo Slider = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/layout_container_bottom_sheet");
        if (Slider == null) {
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            return;
        }

        Log.i(TAG, "Found Slider");
        AccessibilityNodeInfo SliderTitle = HelperFunctions.findNodeByResourceId(rootNode, "com.instagram.android:id/title_text_view");
        AccessibilityNodeInfo confirmationButton = HelperFunctions.findNodeByResourceId(Slider, "com.instagram.android:id/bb_primary_action_container");

        if (SliderTitle == null && SliderTitle.getText() == null || confirmationButton == null) {
            accountManager.BlockCurrentAccount();
            this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            return;
        }

        Log.i(TAG, "Found SliderTitle with title: " + SliderTitle.getText().toString());
        accountManager.setAccountStatus("Private");
        if (SliderTitle.getText().toString().contains("Switch to public account?")){
            Log.e(TAG,"Account is already Private");
            accountManager.BlockCurrentAccount();
            helperFunctions.navigateBack();
            handler.postDelayed(()->this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType)),2000+ random.nextInt(2000));
            return;
        }

        accountManager.BlockCurrentAccount();
        Log.i(TAG, "Found confirmationButton");
        if (confirmationButton.isClickable() && confirmationButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {

            handler.postDelayed(() -> HandlePopUpsforAccountPrivacyChange(() -> {
                this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            }), 4000 + random.nextInt(2000));
        } else {
            this.instagramFollowerBotAutomation.getBoundsAndClick(confirmationButton, () -> HandlePopUpsforAccountPrivacyChange(() -> {
                this.instagramFollowerBotAutomation.getProfileData(() -> this.instagramFollowerBotAutomation.ChangeAccount(this.instagramFollowerBotAutomation::callbackAccordingToType));
            }), "Last", 4000, 2000);
        }
    }

    private void HandlePopUpsforAccountPrivacyChange(Action Callback) {
        if (this.instagramFollowerBotAutomation.shouldContinueAutomation()) {
            return;
        }
        AccessibilityNodeInfo negativeButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/negative_button_row", 5);
        if (negativeButton == null) {
            negativeButton = helperFunctions.FindAndReturnNodeById("com.instagram.android:id/igds_alert_dialog_cancel_button", 2);
            if (negativeButton == null) {
                Callback.execute();
                return;
            }
        }

        Log.i(TAG, "Found PopUp Negative Button");

        if (negativeButton.isClickable() && negativeButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            handler.postDelayed(Callback::execute, 4000 + random.nextInt(2000));
        } else {
            this.instagramFollowerBotAutomation.getBoundsAndClick(negativeButton, Callback, "Center", 4000, 2000);
        }
    }
}
