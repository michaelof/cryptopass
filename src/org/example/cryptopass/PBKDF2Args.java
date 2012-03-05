package org.example.cryptopass;

public final class PBKDF2Args
{
	public String password;
	public String url;
	public String username;

	public Bookmark getBookmark()
	{
		return new Bookmark(url, username);
	}

	public boolean isEmpty()
	{
		return password == null || password.length() == 0;
	}

	public boolean equals(PBKDF2Args args)
	{
		if (password != args.password)
		{
			return false;
		}

		if (url != args.url)
		{
			return false;
		}

		if (username != args.username)
		{
			return false;
		}

		return true;
	}
}
