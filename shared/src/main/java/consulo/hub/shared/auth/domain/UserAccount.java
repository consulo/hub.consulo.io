package consulo.hub.shared.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import consulo.hub.shared.auth.Roles;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(indexes = {
		@Index(columnList = "username", unique = true)
})
public class UserAccount implements UserDetails
{
	public static final int ROLE_SUPERUSER = 1 << 1;
	public static final int ROLE_SUPERDEPLOYER = 1 << 2;

	public static final int ROLE_HUB = 1 << 3;

	@Id
	@GeneratedValue
	@Column
	private Integer id;
	@Column
	private String username;
	@Column
	@JsonIgnore
	private String password;
	@Column
	private String firstname;
	@Column
	private String lastname;
	@Column
	private UserAccountStatus status = UserAccountStatus.STATUS_APPROVED;
	@Column
	private int rights;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	@Override
	public String getUsername()
	{
		return username;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired()
	{
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked()
	{
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired()
	{
		return true;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		if((rights & ROLE_HUB) == ROLE_HUB)
		{
			return List.of(new SimpleGrantedAuthority(Roles.ROLE_HUB));
		}

		List<GrantedAuthority> authorities = new ArrayList<>();

		if((rights & ROLE_SUPERUSER) == ROLE_SUPERUSER)
		{
			authorities.add(new SimpleGrantedAuthority(Roles.ROLE_SUPERUSER));
		}

		if((rights & ROLE_SUPERDEPLOYER) == ROLE_SUPERDEPLOYER)
		{
			authorities.add(new SimpleGrantedAuthority(Roles.ROLE_SUPERDEPLOYER));
		}

		authorities.add(new SimpleGrantedAuthority(Roles.ROLE_USER));
		return authorities;
	}

	@Override
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

	public UserAccountStatus getStatus()
	{
		return status;
	}

	public void setStatus(UserAccountStatus status)
	{
		this.status = status;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled()
	{
		return status != UserAccountStatus.STATUS_DISABLED;
	}

	public int getRights()
	{
		return rights;
	}

	public void setRights(int rights)
	{
		this.rights = rights;
	}

	@Override
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

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().append(id).append(username).toHashCode();
	}
}
