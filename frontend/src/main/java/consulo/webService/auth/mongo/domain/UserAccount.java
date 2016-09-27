package consulo.webService.auth.mongo.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Document
public class UserAccount implements UserDetails
{
	@Id
	private String id;

	@Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
	private String username;

	private String password;
	private String firstname;
	private String lastname;
	private String status;
	private boolean enabled;

	@DBRef
	private List<Role> roles = new ArrayList<Role>();

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	@Override
	public boolean isAccountNonExpired()
	{
		return true;
	}

	@Override
	public boolean isAccountNonLocked()
	{
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired()
	{
		return true;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		return roles.stream().map(role -> new SimpleGrantedAuthority(role.getId())).collect(Collectors.toList());
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getFirstname()
	{
		return firstname;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}

	public String getLastname()
	{
		return lastname;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
	}

	public List<Role> getRoles()
	{
		return roles;
	}

	public void addRole(Role role)
	{
		this.roles.add(role);
	}

	public void removeRole(Role role)
	{
		//use iterator to avoid java.util.ConcurrentModificationException with foreach
		for(Iterator<Role> iter = this.roles.iterator(); iter.hasNext(); )
		{
			if(iter.next().equals(role))
			{
				iter.remove();
			}
		}
	}

	public String getRolesCSV()
	{
		StringBuilder sb = new StringBuilder();
		for(Iterator<Role> iter = this.roles.iterator(); iter.hasNext(); )
		{
			sb.append(iter.next().getId());
			if(iter.hasNext())
			{
				sb.append(',');
			}
		}
		return sb.toString();
	}

	public boolean equals(Object obj)
	{
		if(!(obj instanceof UserAccount))
		{
			return false;
		}
		if(this == obj)
		{
			return true;
		}
		UserAccount rhs = (UserAccount) obj;
		return new EqualsBuilder().append(id, rhs.id).isEquals();
	}

	public int hashCode()
	{
		return new HashCodeBuilder().append(id).append(username).toHashCode();
	}
}
