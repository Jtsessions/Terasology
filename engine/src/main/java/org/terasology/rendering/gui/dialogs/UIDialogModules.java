/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.gui.dialogs;

import com.google.common.collect.Lists;
import org.newdawn.slick.Color;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleSelection;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Immortius
 */
public class UIDialogModules extends UIDialog {
    private static final Color ACTIVE_TEXT_COLOR = new Color(255, 220, 0);
    private static final Color ACTIVE_SELECTED_TEXT_COLOR = new Color(255, 255, 0);
    private static final Color INACTIVE_TEXT_COLOR = new Color(180, 180, 180);
    private static final Color INACTIVE_SELECTED_TEXT_COLOR = new Color(255, 255, 255);
    private static final Color INVALID_TEXT_COLOR = new Color(255, 90, 90);
    private static final Color INVALID_SELECTED_TEXT_COLOR = new Color(255, 0, 0);
    private static final String ACTIVATE_TEXT = "Activate";
    private static final String DEACTIVATE_TEXT = "Deactivate";
    private static final Color BLACK = new Color(0, 0, 0);

    private ModuleSelection selection;
    private UIList modList;
    private UIButton toggleButton;
    private UILabel nameLabel;
    private UILabel descriptionLabel;
    private UILabel versionLabel;
    private UILabel errorLabel;
    private UIComposite detailPanel;
    private ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

    public UIDialogModules(ModuleSelection selection) {
        super(new Vector2f(640f, 480f));
        this.selection = selection;

        this.setEnableScrolling(false);
        populateModList();
        setTitle("Select Modules...");

    }

    public ModuleSelection getSelection() {
        return selection;
    }

    private void populateModList() {
        List<String> modules = Lists.newArrayList(moduleManager.getModuleIds());
        Collections.sort(modules, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                Module mod1 = moduleManager.getLatestModuleVersion(o1);
                Module mod2 = moduleManager.getLatestModuleVersion(o2);
                return mod1.getModuleInfo().getDisplayName().compareTo(mod2.getModuleInfo().getDisplayName());
            }
        });

        for (String moduleId : modules) {
            if (!moduleId.equals(TerasologyConstants.ENGINE_MODULE)) {
                Module module = moduleManager.getLatestModuleVersion(moduleId);
                UIListItem item = new UIListItem(module.getModuleInfo().getDisplayName(), module);
                item.setPadding(new Vector4f(2f, 5f, 2f, 5f));
                if (selection.contains(module.getId())) {
                    item.setTextColor(ACTIVE_TEXT_COLOR);
                    item.setTextSelectionColor(ACTIVE_SELECTED_TEXT_COLOR);
                } else if (selection.add(module.getId()).isValid()) {
                    item.setTextColor(INACTIVE_TEXT_COLOR);
                    item.setTextSelectionColor(INACTIVE_SELECTED_TEXT_COLOR);
                } else {
                    item.setTextColor(INVALID_TEXT_COLOR);
                    item.setTextSelectionColor(INVALID_SELECTED_TEXT_COLOR);
                }
                modList.addItem(item);
            }
        }
        modList.addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                Module module = (Module) modList.getSelection().getValue();
                detailPanel.setVisible(true);
                nameLabel.setText(module.getModuleInfo().getDisplayName());
                versionLabel.setText(module.getVersion().toString());
                ModuleSelection tryAddModule = selection.add(module);
                if (!tryAddModule.isValid()) {
                    errorLabel.setText(tryAddModule.getValidationMessages().get(0));
                } else {
                    errorLabel.setText("");
                }
                descriptionLabel.setText(module.getModuleInfo().getDescription());
                boolean active = selection.contains(module.getId());
                if (tryAddModule.isValid()) {
                    if (active) {
                        toggleButton.getLabel().setText(DEACTIVATE_TEXT);
                    } else {
                        toggleButton.getLabel().setText(ACTIVATE_TEXT);
                    }
                } else {
                    toggleButton.setVisible(false);
                }
            }
        });
        modList.addDoubleClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                toggleSelectedModuleActivation();
            }
        });

        toggleButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                toggleSelectedModuleActivation();
            }
        });
    }

    private void toggleSelectedModuleActivation() {
        Module selectedModule = (Module) modList.getSelection().getValue();
        if (selectedModule.getId().equals("core")) {
            return;
        }
        if (selection.contains(selectedModule.getId())) {
            selection = selection.remove(selectedModule.getId());
            refreshListItemActivation();
            toggleButton.getLabel().setText(ACTIVATE_TEXT);
        } else {
            ModuleSelection newSelection = selection.add(selectedModule);
            if (newSelection.isValid()) {
                selection = newSelection;
                refreshListItemActivation();
                toggleButton.getLabel().setText(DEACTIVATE_TEXT);
            }
        }
    }

    private void refreshListItemActivation() {
        for (UIListItem item : modList.getItems()) {
            Module module = (Module) item.getValue();
            if (selection.contains(module.getId())) {
                item.setTextColor(ACTIVE_TEXT_COLOR);
                item.setTextSelectionColor(ACTIVE_SELECTED_TEXT_COLOR);
            } else if (selection.add(module.getId()).isValid()) {
                item.setTextColor(INACTIVE_TEXT_COLOR);
                item.setTextSelectionColor(INACTIVE_SELECTED_TEXT_COLOR);
            } else {
                item.setTextColor(INVALID_TEXT_COLOR);
                item.setTextSelectionColor(INVALID_SELECTED_TEXT_COLOR);
            }
        }
    }

    @Override
    protected void createDialogArea(UIDisplayContainer parent) {

        UIComposite modPanel = new UIComposite();
        modPanel.setPosition(new Vector2f(15, 50f));
        modPanel.setSize(new Vector2f(320f, 400f));
        modPanel.setVisible(true);

        detailPanel = new UIComposite();
        detailPanel.setPosition(new Vector2f(340, 50));
        detailPanel.setSize(new Vector2f(320, 400));
        detailPanel.setVisible(true);

        modList = new UIList();
        modList.setVisible(true);
        modList.setSize(new Vector2f(300f, 350f));
        modList.setPadding(new Vector4f(10f, 5f, 10f, 5f));
        modList.setBackgroundImage("engine:gui_menu", new Vector2f(264f, 18f), new Vector2f(159f, 63f));
        modList.setBorderImage("engine:gui_menu", new Vector2f(256f, 0f), new Vector2f(175f, 88f), new Vector4f(16f, 7f, 7f, 7f));

        modPanel.addDisplayElement(modList);
        modPanel.layout();

        UILabel label = new UILabel("Name:");
        label.setVisible(true);
        label.setPosition(new Vector2f(0, 0));
        label.setColor(BLACK);
        detailPanel.addDisplayElement(label);
        nameLabel = new UILabel();
        nameLabel.setVisible(true);
        nameLabel.setColor(BLACK);
        nameLabel.setTextShadow(false);
        nameLabel.setPosition(new Vector2f(label.getPosition().x + label.getSize().x + 10f, label.getPosition().y));
        detailPanel.addDisplayElement(nameLabel);
        label = new UILabel("Version:");
        label.setVisible(true);
        label.setPosition(new Vector2f(0, nameLabel.getPosition().y + nameLabel.getSize().y + 8f));
        label.setColor(BLACK);
        detailPanel.addDisplayElement(label);
        versionLabel = new UILabel();
        versionLabel.setColor(BLACK);
        versionLabel.setTextShadow(false);
        versionLabel.setPosition(new Vector2f(label.getPosition().x + label.getSize().x + 10f, label.getPosition().y));
        detailPanel.addDisplayElement(versionLabel);

        errorLabel = new UILabel();
        errorLabel.setColor(Color.red);
        errorLabel.setTextShadow(false);
        errorLabel.setPosition(new Vector2f(0, versionLabel.getPosition().y + versionLabel.getSize().y + 8f));
        detailPanel.addDisplayElement(errorLabel);

        label = new UILabel("Description:");
        label.setVisible(true);
        label.setPosition(new Vector2f(0, errorLabel.getPosition().y + errorLabel.getSize().y + 8f));
        label.setColor(BLACK);
        detailPanel.addDisplayElement(label);


        descriptionLabel = new UILabel();
        descriptionLabel.setColor(BLACK);
        descriptionLabel.setVisible(true);
        descriptionLabel.setWrap(true);
        descriptionLabel.setTextShadow(false);
        descriptionLabel.setPosition(new Vector2f(0, label.getPosition().y + label.getSize().y + 8f));
        descriptionLabel.setSize(new Vector2f(300, descriptionLabel.getSize().y));
        detailPanel.addDisplayElement(descriptionLabel);

        toggleButton = new UIButton(new Vector2f(128f, 32), UIButton.ButtonType.NORMAL);
        toggleButton.setPosition(new Vector2f(0, 240f));
        toggleButton.setVisible(true);

        detailPanel.addDisplayElement(toggleButton);
        detailPanel.setVisible(false);

        addDisplayElement(modPanel);
        addDisplayElement(detailPanel);

    }

    @Override
    protected void createButtons(UIDisplayContainer parent) {
        UIButton okButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        okButton.getLabel().setText("Ok");
        okButton.setPosition(new Vector2f(getSize().x / 2 - okButton.getSize().x - 16f, getSize().y - okButton.getSize().y - 10));
        okButton.setVisible(true);

        okButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                closeDialog(EReturnCode.OK, selection);
            }
        });

        UIButton cancelButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        cancelButton.setPosition(new Vector2f(okButton.getPosition().x + okButton.getSize().x + 16f, okButton.getPosition().y));
        cancelButton.getLabel().setText("Cancel");
        cancelButton.setVisible(true);

        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });

        parent.addDisplayElement(okButton);
        parent.addDisplayElement(cancelButton);
    }
}
