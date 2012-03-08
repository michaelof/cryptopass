package org.example.cryptopass;

import android.text.TextUtils;

public final class PBKDF2Args
{
	public String password;
	public String url;
	public String username;

	public PBKDF2Args dropSecret() {
		PBKDF2Args args = new PBKDF2Args();
		
		args.url = url;
		args.username = username;
		
		return args;
	}
	
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
	
	public String toString() {
		if (TextUtils.isEmpty(password)) {
			return String.format("PBKDF2Args: %1$s @ %2$s", username, url);
		}
		
		return  String.format("PBKDF2Args: %1$s @ %2$s with secret", username, url);
	}
}
