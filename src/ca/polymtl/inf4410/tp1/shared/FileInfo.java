package ca.polymtl.inf4410.tp1.shared;

import java.io.Serializable;
import java.util.UUID;

public class FileInfo implements Serializable, Comparable<FileInfo> {
    private String m_name;
    private byte[] m_content;
    private UUID m_lockedUser;

    public FileInfo(String name) {
        this(name, null);
    }

    public FileInfo(String name, byte[] content){
        setName(name);
        setContent(content);
        setLockedUser(null);
    }

    public void setName(String name) {
        m_name = name;
    }
    
    public String getName() {
        return m_name;
    }

    public byte[] getContent() {
        return m_content;
    }

    public void setContent(byte[] content){
        m_content = content != null ? content : new byte[0];
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
    
    @Override
    public int compareTo(FileInfo other) {
	return this.getName().compareToIgnoreCase(other.getName());
    }
}