package org.example.cryptopass;

final class Bookmark
{
	public final String url;
	public final String username;

	public Bookmark(String url, String username)
	{
		this.url = url;
		this.username = username;
	}

	public boolean isEmpty()
	{
		if (url != null && url.length() > 0)
		{
			return false;
		}

		if (username != null && username.length() == 0)
		{
			return true;
		}

		return false;
	}
}
