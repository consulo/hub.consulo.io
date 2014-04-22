package org.mustbe.consulo.war.plugins;

import java.io.File;

import org.jdom.Document;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.NotNullLazyValue;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public class StaticPluginDirManager extends PluginDirManager
{
	private NotNullLazyValue<String> myText = new NotNullLazyValue<String>()
	{
		@NotNull
		@Override
		protected String compute()
		{
			try
			{
				Document document = JDOMUtil.loadDocument(new File("list.xml"));
				return JDOMUtil.writeDocument(document, "\n");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return EMPTY;
			}
		}
	};

	public StaticPluginDirManager(File file)
	{
		super(file);
	}

	@NotNull
	@Override
	public String getXmlListText()
	{
		return myText.getValue();
	}
}
