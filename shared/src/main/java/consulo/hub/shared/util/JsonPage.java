package consulo.hub.shared.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * @author VISTALL
 * @since 29/08/2021
 */
public class JsonPage<T>
{
	public static <K> Page<K> from(JsonPage<K> jsonPage, int pageSize)
	{
		return new PageImpl<>(jsonPage.getItems(), new PageRequest(jsonPage.getPage(), pageSize), jsonPage.getTotalElements());
	}

	private int totalPages;
	private int totalElements;
	private int page;
	private List<T> items;

	public JsonPage()
	{
	}

	public JsonPage(Page<T> page)
	{
		this.page = page.getNumber();
		this.totalPages = page.getTotalPages();
		this.totalElements = (int) page.getTotalElements();
		this.items = page.getContent();
	}

	public int getTotalPages()
	{
		return totalPages;
	}

	public void setTotalPages(int totalPages)
	{
		this.totalPages = totalPages;
	}

	public int getPage()
	{
		return page;
	}

	public void setPage(int page)
	{
		this.page = page;
	}

	public List<T> getItems()
	{
		return items;
	}

	public void setItems(List<T> items)
	{
		this.items = items;
	}

	public int getTotalElements()
	{
		return totalElements;
	}

	public void setTotalElements(int totalElements)
	{
		this.totalElements = totalElements;
	}
}
