package ca.polymtl.inf4410.tp1.shared;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private String m_name;
    private byte[] m_content;
    private boolean m_locked;
    private String m_lockedUser;

    public FileInfo(String name) {
        this(name, null);
    }

    public FileInfo(String name, byte[] content){
        m_name = name;
        m_content = content;
        m_locked = false;
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

    public boolean getLocked() {
        return m_locked;
    }

    public void setLocked(boolean locked) {
        m_locked = locked;
    }

    public String getLockedUser() {
        return m_lockedUser;
    }

    public void setLockedUser(String user){
        m_lockedUser = user;
    }

    public boolean Lock(String user){
        if (!m_locked && (m_lockedUser == null || !m_lockedUser.isEmpty())) {
            setLocked(true);
            setLockedUser(user);
            return true;
        }
        return false;
    }

    public void Unlock(){
        setLocked(false);
        setLockedUser(null);
    }
}