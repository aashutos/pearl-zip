/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Optional;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_ICON_REF;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.fileIcon;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.folderIcon;

/**
 *  Implementation of cell renderer for the Name field. This has additional functionality to include a file icon (for
 *  folder or file type by default).
 *  @author Aashutos Kakshepati
*/
public class NameHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {
    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        if (info != null) {
            //Set up the ImageView
            final ImageView icon = new ImageView();
            icon.setFitHeight(16);
            icon.setFitWidth(16);
            icon.setImage(fileIcon);

            String iconRef;
            // Override icon
            if (!(iconRef =
                    Optional.ofNullable(info.getAdditionalInfoMap()
                                            .get(KEY_ICON_REF))
                            .orElse("")
                            .toString()).isEmpty()) {
                    icon.setImage(new Image(iconRef));
            } else { // Use default values
                if (info.isFolder()) {
                    icon.setImage(folderIcon);
                }
            }

            String text = info.getFileName();
            if (text.contains("/")) {
                cell.setText(text.split("/")[text.split("/").length - 1]);
            } else {
                cell.setText(text);
            }

            cell.setGraphic(icon);
        }
    }
}
