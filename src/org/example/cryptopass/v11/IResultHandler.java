package org.example.cryptopass.v11;

import org.example.cryptopass.Bookmark;

public interface IResultHandler
{
	void exception(Exception occurredException);

	void complete(Bookmark args, String result);

	void empty();
}
