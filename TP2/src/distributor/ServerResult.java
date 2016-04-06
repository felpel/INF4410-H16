package distributor;

//Custom class that serves as a container for a result from a calculation server 

public class ServerResult {
  private Integer m_computedResult = null;
  private Exception m_failure = null;
  private int m_serverId;

  public ServerResult() {}

  public void setResult(Integer i) {
    this.m_computedResult = i;
  }
  
  public void setServerId(int id) {
    this.m_serverId = id;
  }

  public void setFailure(Exception e) {
    this.m_failure = e;
  }

  public Integer getResult() {return this.m_computedResult;}
  public Exception getFailure() {return this.m_failure;}
  public int getServerId() {return this.m_serverId;}
}
