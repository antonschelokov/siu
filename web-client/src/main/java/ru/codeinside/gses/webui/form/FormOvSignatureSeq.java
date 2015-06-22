/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2015, MPL CodeInside http://codeinside.ru
 */

package ru.codeinside.gses.webui.form;

import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.VerticalLayout;
import ru.codeinside.gses.activiti.forms.FormID;
import ru.codeinside.gses.activiti.forms.Signatures;
import ru.codeinside.gses.webui.form.api.FieldSignatureSource;
import ru.codeinside.gses.webui.wizard.TransitionAction;

import javax.xml.soap.SOAPMessage;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FormOvSignatureSeq extends AbstractFormSeq {

  public static final String OV_SIGN = "SoapBodySignatureField";
  public static final String SIGNED_DATA_ID = "SignedSoapBody";
  public static final String SOAP_MESSAGE_ID = "SignedSoapMessageId";
  public static final String REQUEST_ID = "ClientRequestEntityId";

  private Form form;

  public FormOvSignatureSeq(DataAccumulator dataAccumulator) {
    super(dataAccumulator);
  }

  @Override
  public String getCaption() {
    return "Подписание тела запроса подписью ОВ";
  }

  /**
   * Заполненные поля в порядке заполнения.
   */
  @Override
  public List<FormField> getFormFields() {
    List<FormField> fields = new ArrayList<FormField>();
    if (form != null) {
      Collection<?> propertyIds = form.getItemPropertyIds();
      for (Object propertyId : propertyIds) {
        Field field = form.getField(propertyId);
        fields.add(new BasicFormField(propertyId, field.getCaption(), field.getValue()));
      }
    }
    return Collections.unmodifiableList(fields);
  }

  /**
   * Создание формы на основе предыдущей.
   *
   * @param formId
   * @param previous
   */
  @Override
  public Form getForm(FormID formId, FormSeq previous) {
    List<Long> entityId = null;
    if (dataAccumulator.getServiceName() != null) {
      entityId = dataAccumulator.getRequestId();
    } else if (dataAccumulator.getRequestType() != null) {
      entityId = dataAccumulator.getResponseId();
    }
    form = new OvSignatureForm(dataAccumulator.getSoapMessage(), entityId);

    byte[] signDataBytes = (byte[]) resultTransition.getData();
    String signData = new String(signDataBytes, Charset.forName("UTF-8"));

    FormUtils.addSignedDataToForm(form, signData, SIGNED_DATA_ID);
    FormUtils.addSignatureFieldToForm(form, formId, signData, OV_SIGN, dataAccumulator);

    return form;
  }

  @Override
  public TransitionAction getTransitionAction(List<FormField> formFields) {
    if (dataAccumulator.getClientRequest() == null) {
      dataAccumulator.setFormFields(formFields);
    }
    if (dataAccumulator.getServiceName() != null) {
      return new CreateSoapMessageAction(dataAccumulator);
    } else if (dataAccumulator.getRequestType() != null) {
      return new CreateResultSoapMessageAction(dataAccumulator);
    } else {
      throw new IllegalStateException("Ошибка в маршруте");
    }
  }

  final public static class OvSignatureForm extends Form implements FieldSignatureSource {

    private List<SOAPMessage> soapMessage;
    private List<Long> entityId;// List нужен для того, что бы entityId был mutable. Там всегда один элемент

    public OvSignatureForm(List<SOAPMessage> soapMessage, List<Long> entityId) {
      this.setDescription("Электронная подпись предназначена для идентификации лица, " +
          "подписавшего электронный документ и является полноценной заменой (аналогом) " +
          "собственноручной подписи в случаях, предусмотренных Гражданским кодексом Российской Федерации " +
          "(часть 1, глава 9, статья 160)");
      this.soapMessage = soapMessage;
      this.entityId = entityId;
    }

    public String getSoapMessage() {
      return FormUtils.persistSoapMessage(soapMessage.get(0));
    }

    public Long getEntityId() {
      return entityId.get(0);
    }

    @Override
    public String getSignedData() {
      Field f = getField(SIGNED_DATA_ID);
      return (String) f.getValue();
    }

    @Override
    public Signatures getSignatures() {
      Field field = getField(FormSignatureSeq.SIGNATURE);
      Object value = field.getValue();
      return value instanceof Signatures ? (Signatures) value : null;
    }

    @Override
    public void attach() {
      super.attach();
      VerticalLayout vl = (VerticalLayout) getParent();
      vl.setWidth(100, UNITS_PERCENTAGE);
      vl.setHeight(-1, UNITS_PIXELS);
    }
  }
}
