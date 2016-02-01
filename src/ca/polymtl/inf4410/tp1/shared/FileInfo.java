package ca.polymtl.inf4410.tp1.shared;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private String m_name;
    private byte[] m_content;
    private UUID m_lockedUser;

    public FileInfo(String name) {
        this(name, null);
    }

    public FileInfo(String name, byte[] content){
        m_name = name;
        m_content = content;
        m_lockedUser = null;
    }

    public String getName() {
        return m_name;
    }

    public byte[] getContent() {
        return m_content;
    }

    public void setContent(byte[] content){
        m_content = content;
    }

    public UUID getLockedUser() {
        return m_lockedUser;
    }

    public void setLockedUser(UUID user){
        m_lockedUser = user;
    }

    public boolean lock(UUID user){
        if (m_lockedUser == null) {
            setLockedUser(user);
            return true;
        }
        return false;
    }

    public void unlock(){
        setLockedUser(null);
    }
}