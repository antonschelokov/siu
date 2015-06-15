package ru.codeinside.gses.webui.form;

import ru.codeinside.gses.activiti.forms.Signatures;
import ru.codeinside.gws.api.Client;
import ru.codeinside.gws.api.ClientRequest;
import ru.codeinside.gws.api.Server;
import ru.codeinside.gws.api.ServerResponse;

import javax.xml.soap.SOAPMessage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Накапливает данные в результат выполнения шагов мастера. Используется пока как черновое решение.
 */
public class DataAccumulator implements Serializable {
  private boolean needOv;
  private Client client;
  private Server server;
  private String serviceName;
  private String taskId;

  private ClientRequest clientRequest;
  private ServerResponse serverResponse;
  private List<FormField> formFields;
  private Signatures spSignature;
  private Signatures ovSignatures;

  //List нужен, чтобы можно было меня значение, сохраняя ссылку. В списке всегда один элемент
  private List<Long> requestId;
  private List<SOAPMessage> soapMessage;

  private String requestType;

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public ClientRequest getClientRequest() {
    return clientRequest;
  }

  public void setClientRequest(ClientRequest clientRequest) {
    this.clientRequest = clientRequest;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public List<FormField> getFormFields() {
    return formFields;
  }

  public void setFormFields(List<FormField> formFields) {
    this.formFields = formFields;
  }

  public Signatures getSpSignature() {
    return spSignature;
  }

  public void setSpSignature(Signatures spSignature) {
    this.spSignature = spSignature;
  }

  public Signatures getOvSignatures() {
    return ovSignatures;
  }

  public void setOvSignatures(Signatures ovSignatures) {
    this.ovSignatures = ovSignatures;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public List<Long> getRequestId() {
    return requestId;
  }

  //всегда хранится только одно значение. Нужно, что бы значение было mutable
  public void setRequestId(Long requestId) {
    if (this.requestId == null) {
      this.requestId = new ArrayList<Long>();
      this.requestId.add(requestId);
    }
    this.requestId.set(0, requestId);
  }

  public boolean isNeedOv() {
    return needOv;
  }

  public void setNeedOv(boolean needOv) {
    this.needOv = needOv;
  }

  public List<SOAPMessage> getSoapMessage() {
    return soapMessage;
  }

  public void setSoapMessage(SOAPMessage soapMessage) {
    if (this.soapMessage == null) {
      this.soapMessage = new ArrayList<SOAPMessage>();
      this.soapMessage.add(soapMessage);
    }
    this.soapMessage.set(0, soapMessage);
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  public ServerResponse getServerResponse() {
    return serverResponse;
  }

  public void setServerResponse(ServerResponse serverResponse) {
    this.serverResponse = serverResponse;
  }
}
