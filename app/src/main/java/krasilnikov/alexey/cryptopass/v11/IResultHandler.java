package krasilnikov.alexey.cryptopass.v11;

import krasilnikov.alexey.cryptopass.Bookmark;

public interface IResultHandler
{
	void exception(Exception occurredException);

	void complete(Bookmark args, String result);

	void empty();
}
