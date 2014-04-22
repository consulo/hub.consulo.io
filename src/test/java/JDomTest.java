import org.jdom.Document;
import org.jdom.Element;
import com.intellij.openapi.util.JDOMUtil;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class JDomTest
{
	public static void main(String[] args)
	{
		Document document = new Document();
		document.addContent(new Element("root").setAttribute("test", "true").addContent(new Element("b")));

		System.out.println(	JDOMUtil.writeDocument(document, "\n"));
	}
}
