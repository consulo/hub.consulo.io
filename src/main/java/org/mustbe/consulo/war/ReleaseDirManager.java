package org.mustbe.consulo.war;

import java.io.File;

import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 22.04.14
 */
public interface ReleaseDirManager
{
	@NotNull
	File getDir();
}
