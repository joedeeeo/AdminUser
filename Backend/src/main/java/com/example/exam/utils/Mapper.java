package com.example.exam.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.exam.entity.AdminUser;
import com.example.exam.proxy.AdminUserProxy;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Mapper {
	
	@Autowired
	private ObjectMapper mapper;
	
	public AdminUser proxyToEntity(AdminUserProxy p)
	{
		return mapper.convertValue(p, AdminUser.class);
	}
	
	public AdminUserProxy entityToProxy(AdminUser e)
	{
		e.setProfileImage(null);
		return mapper.convertValue(e, AdminUserProxy.class);
	}
	
	public List<AdminUserProxy> entityListToProxyList(List<AdminUser> list)
	{
		return list.stream().map(entity -> entityToProxy(entity)).collect(Collectors.toList());
	}
	
}
