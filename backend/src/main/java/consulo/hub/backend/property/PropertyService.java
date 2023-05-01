package consulo.hub.backend.property;

import consulo.hub.backend.property.domain.Property;
import consulo.hub.backend.property.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author VISTALL
 * @since 28/08/2021
 */
@Service
public class PropertyService
{
	private PropertyRepository myPropertyRepository;

	@Autowired
	public PropertyService(PropertyRepository propertyRepository)
	{
		myPropertyRepository = propertyRepository;
	}

	public boolean getValue(String name)
	{
		Property property = myPropertyRepository.findById(name).orElse(null);
		if(property != null)
		{
			return Boolean.valueOf(property.getValue());
		}
		return false;
	}

	public void setValue(String name, boolean value)
	{
		Property property = myPropertyRepository.findById(name).orElse(null);
		if(property == null)
		{
			property = new Property();
			property.setKey(name);
		}

		property.setValue(String.valueOf(value));

		myPropertyRepository.save(property);
	}
}
