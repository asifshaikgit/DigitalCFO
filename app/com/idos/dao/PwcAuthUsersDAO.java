package com.idos.dao;

import java.util.List;

import model.PwcAuthUsers;

public interface PwcAuthUsersDAO {
    public void save(PwcAuthUsers user);
    public void update(PwcAuthUsers user);
    public void delete(PwcAuthUsers user);
    public PwcAuthUsers findById(int id);
    public List<PwcAuthUsers> findAll();
}
