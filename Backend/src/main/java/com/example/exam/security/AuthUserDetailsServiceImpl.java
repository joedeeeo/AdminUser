package com.example.exam.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.exam.entity.AdminUser;
import com.example.exam.repo.AdminUserRepo;


@Service
public class AuthUserDetailsServiceImpl implements UserDetailsService{
	
	@Autowired
	private AdminUserRepo repo;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if(repo.findByEmail(username).isEmpty()) throw new UsernameNotFoundException(username);
		
		AdminUser user = repo.findByEmail(username).get();
		
		
		UserDetails userDetail = new UserDetails() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getUsername() {
				// TODO Auto-generated method stub
				return user.getEmail();
			}
			
			@Override
			public String getPassword() {
				// TODO Auto-generated method stub
				return user.getPassword();
			}
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				// TODO Auto-generated method stub
				List<GrantedAuthority> listAuthorities = new ArrayList<GrantedAuthority>();
				
				listAuthorities.add(new SimpleGrantedAuthority(user.getRole().toString()));
				
				return listAuthorities;
			}
		};
		// TODO Auto-generated method stub
		System.err.println(userDetail.getUsername());
		return userDetail;
	}

}
