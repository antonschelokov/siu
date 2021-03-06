
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2013, MPL CodeInside http://codeinside.ru
 */

package ru.gosuslugi.smev.rev111111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import ru.fccland.portal.types.GetStatusRequestMessageType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getStatusRequestMessageData", propOrder = {
    "appData",
    "appDocument"
})
public class GetStatusRequestMessageData {

    @XmlElement(name = "AppData", required = true)
    protected GetStatusRequestMessageType appData;
    @XmlElement(name = "AppDocument")
    protected AppDocumentType appDocument;

    /**
     * Gets the value of the appData property.
     * 
     * @return
     *     possible object is
     *     {@link GetStatusRequestMessageType }
     *     
     */
    public GetStatusRequestMessageType getAppData() {
        return appData;
    }

    /**
     * Sets the value of the appData property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetStatusRequestMessageType }
     *     
     */
    public void setAppData(GetStatusRequestMessageType value) {
        this.appData = value;
    }

    /**
     * Gets the value of the appDocument property.
     * 
     * @return
     *     possible object is
     *     {@link AppDocumentType }
     *     
     */
    public AppDocumentType getAppDocument() {
        return appDocument;
    }

    /**
     * Sets the value of the appDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppDocumentType }
     *     
     */
    public void setAppDocument(AppDocumentType value) {
        this.appDocument = value;
    }

}
