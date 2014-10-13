// $ANTLR 3.4 Tokenizer.g 2013-09-07 10:41:09
package edu.isi.karma.cleaning;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings({ "all", "warnings", "unchecked" })
public class Tokenizer extends Lexer {
	public static final int EOF = -1;
	public static final int BLANK = 4;
	public static final int COL = 5;
	public static final int DIGIT = 6;
	public static final int END = 7;
	public static final int LETTER = 8;
	public static final int LOWER = 9;
	public static final int LWRD = 10;
	public static final int NUMBER = 11;
	public static final int ROW = 12;
	public static final int START = 13;
	public static final int SYBS = 14;
	public static final int SYMBOL = 15;
	public static final int UPPER = 16;
	public static final int UWRD = 17;

	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public Tokenizer() {
	}

	public Tokenizer(CharStream input) {
		this(input, new RecognizerSharedState());
	}

	public Tokenizer(CharStream input, RecognizerSharedState state) {
		super(input, state);
	}

	public String getGrammarFileName() {
		return "Tokenizer.g";
	}

	public Token nextToken() {
		while (true) {
			if (input.LA(1) == CharStream.EOF) {
				Token eof = new CommonToken((CharStream) input, Token.EOF,
						Token.DEFAULT_CHANNEL, input.index(), input.index());
				eof.setLine(getLine());
				eof.setCharPositionInLine(getCharPositionInLine());
				return eof;
			}
			state.token = null;
			state.channel = Token.DEFAULT_CHANNEL;
			state.tokenStartCharIndex = input.index();
			state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			state.tokenStartLine = input.getLine();
			state.text = null;
			try {
				int m = input.mark();
				state.backtracking = 1;
				state.failed = false;
				mTokens();
				state.backtracking = 0;
				if (state.failed) {
					input.rewind(m);
					input.consume();
				} else {
					emit();
					return state.token;
				}
			} catch (RecognitionException re) {
				// shouldn't happen in backtracking mode, but...
				reportError(re);
				recover(re);
			}
		}
	}

	public void memoize(IntStream input, int ruleIndex, int ruleStartIndex) {
		if (state.backtracking > 1)
			super.memoize(input, ruleIndex, ruleStartIndex);
	}

	public boolean alreadyParsedRule(IntStream input, int ruleIndex) {
		if (state.backtracking > 1)
			return super.alreadyParsedRule(input, ruleIndex);
		return false;
	}

	// $ANTLR start "BLANK"
	public final void mBLANK() throws RecognitionException {
		try {
			int _type = BLANK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:4:6: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\ ' )+ )
			// Tokenizer.g:4:8: ( '\\t' | ' ' | '\\r' | '\\n' | '\ ' )+
			{
				// Tokenizer.g:4:8: ( '\\t' | ' ' | '\\r' | '\\n' | '\ ' )+
				int cnt1 = 0;
				loop1: do {
					int alt1 = 2;
					int LA1_0 = input.LA(1);

					if (((LA1_0 >= '\t' && LA1_0 <= '\n')
							|| (LA1_0 >= '\f' && LA1_0 <= '\r') || LA1_0 == ' ')) {
						alt1 = 1;
					}

					switch (alt1) {
					case 1:
					// Tokenizer.g:
					{
						if ((input.LA(1) >= '\t' && input.LA(1) <= '\n')
								|| (input.LA(1) >= '\f' && input.LA(1) <= '\r')
								|| input.LA(1) == ' ') {
							input.consume();
							state.failed = false;
						} else {
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							MismatchedSetException mse = new MismatchedSetException(
									null, input);
							recover(mse);
							throw mse;
						}

					}
						break;
					default:
						if (cnt1 >= 1)
							break loop1;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(1,
								input);
						throw eee;
					}
					cnt1++;
				} while (true);
			}
			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BLANK"

	// $ANTLR start "UWRD"
	public final void mUWRD() throws RecognitionException {
		try {
			int _type = UWRD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:5:6: ( UPPER )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z')
						|| (input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')
						|| (input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00DE')) {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "UWRD"

	// $ANTLR start "LWRD"
	public final void mLWRD() throws RecognitionException {
		try {
			int _type = LWRD;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:6:6: ( ( LOWER )+ )
			// Tokenizer.g:6:8: ( LOWER )+
			{
				// Tokenizer.g:6:8: ( LOWER )+
				int cnt1 = 0;
				loop1: do {
					int alt1 = 2;
					int LA1_0 = input.LA(1);

					if (((LA1_0 >= 'a' && LA1_0 <= 'z')
							|| (LA1_0 >= '\u00DF' && LA1_0 <= '\u00F6') || (LA1_0 >= '\u00F8' && LA1_0 <= '\u00FF'))) {
						alt1 = 1;
					}

					switch (alt1) {
					case 1:
					// Tokenizer.g:
					{
						if ((input.LA(1) >= 'a' && input.LA(1) <= 'z')
								|| (input.LA(1) >= '\u00DF' && input.LA(1) <= '\u00F6')
								|| (input.LA(1) >= '\u00F8' && input.LA(1) <= '\u00FF')) {
							input.consume();
							state.failed = false;
						} else {
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							MismatchedSetException mse = new MismatchedSetException(
									null, input);
							recover(mse);
							throw mse;
						}

					}
						break;

					default:
						if (cnt1 >= 1)
							break loop1;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(1,
								input);
						throw eee;
					}
					cnt1++;
				} while (true);

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "LWRD"

	// $ANTLR start "NUMBER"
	public final void mNUMBER() throws RecognitionException {
		try {
			int _type = NUMBER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:7:7: ( ( DIGIT )+ )
			// Tokenizer.g:7:9: ( DIGIT )+
			{
				// Tokenizer.g:7:9: ( DIGIT )+
				int cnt2 = 0;
				loop2: do {
					int alt2 = 2;
					int LA2_0 = input.LA(1);

					if (((LA2_0 >= '0' && LA2_0 <= '9'))) {
						alt2 = 1;
					}

					switch (alt2) {
					case 1:
					// Tokenizer.g:
					{
						if ((input.LA(1) >= '0' && input.LA(1) <= '9')) {
							input.consume();
							state.failed = false;
						} else {
							if (state.backtracking > 0) {
								state.failed = true;
								return;
							}
							MismatchedSetException mse = new MismatchedSetException(
									null, input);
							recover(mse);
							throw mse;
						}

					}
						break;

					default:
						if (cnt2 >= 1)
							break loop2;
						if (state.backtracking > 0) {
							state.failed = true;
							return;
						}
						EarlyExitException eee = new EarlyExitException(2,
								input);
						throw eee;
					}
					cnt2++;
				} while (true);

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "NUMBER"

	// $ANTLR start "SYBS"
	public final void mSYBS() throws RecognitionException {
		try {
			int _type = SYBS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:8:5: ( ( SYMBOL ) )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= '!' && input.LA(1) <= '/')
						|| (input.LA(1) >= ':' && input.LA(1) <= '@')
						|| (input.LA(1) >= '[' && input.LA(1) <= '`')
						|| (input.LA(1) >= '{' && input.LA(1) <= '~')
						|| input.LA(1) == '€') {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "SYBS"

	// $ANTLR start "START"
	public final void mSTART() throws RecognitionException {
		try {
			int _type = START;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:9:7: ( '<_START>' )
			// Tokenizer.g:9:9: '<_START>'
			{
				match("<_START>");
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "START"

	// $ANTLR start "END"
	public final void mEND() throws RecognitionException {
		try {
			int _type = END;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:10:5: ( '<_END>' )
			// Tokenizer.g:10:7: '<_END>'
			{
				match("<_END>");
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "END"

	// $ANTLR start "COL"
	public final void mCOL() throws RecognitionException {
		try {
			int _type = COL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:11:5: ( '<_COL>' )
			// Tokenizer.g:11:7: '<_COL>'
			{
				match("<_COL>");
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "COL"

	// $ANTLR start "ROW"
	public final void mROW() throws RecognitionException {
		try {
			int _type = ROW;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// Tokenizer.g:12:5: ( '<_ROW>' )
			// Tokenizer.g:12:7: '<_ROW>'
			{
				match("<_ROW>");
				if (state.failed)
					return;

			}

			state.type = _type;
			state.channel = _channel;
		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "ROW"

	// $ANTLR start "SYMBOL"
	public final void mSYMBOL() throws RecognitionException {
		try {
			// Tokenizer.g:15:2: ( '!' .. '/' | ':' .. '@' | '[' .. '`' | '{' ..
			// '~' | '???' )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= '!' && input.LA(1) <= '/')
						|| (input.LA(1) >= ':' && input.LA(1) <= '@')
						|| (input.LA(1) >= '[' && input.LA(1) <= '`')
						|| (input.LA(1) >= '{' && input.LA(1) <= '~')
						|| input.LA(1) == '€') {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "SYMBOL"

	// $ANTLR start "LETTER"
	public final void mLETTER() throws RecognitionException {
		try {
			// Tokenizer.g:17:2: ( LOWER | UPPER )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z')
						|| (input.LA(1) >= 'a' && input.LA(1) <= 'z')
						|| (input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')
						|| (input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')
						|| (input.LA(1) >= '\u00F8' && input.LA(1) <= '\u00FF')) {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "LETTER"

	// $ANTLR start "LOWER"
	public final void mLOWER() throws RecognitionException {
		try {
			// Tokenizer.g:19:2: ( '\\u0061' .. '\\u007a' | '\\u00df' ..
			// '\\u00f6' | '\\u00f8' .. '\\u00ff' )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= 'a' && input.LA(1) <= 'z')
						|| (input.LA(1) >= '\u00DF' && input.LA(1) <= '\u00F6')
						|| (input.LA(1) >= '\u00F8' && input.LA(1) <= '\u00FF')) {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "LOWER"

	// $ANTLR start "UPPER"
	public final void mUPPER() throws RecognitionException {
		try {
			// Tokenizer.g:21:2: ( 'A' .. 'Z' )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= 'A' && input.LA(1) <= 'Z')
						|| (input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')
						|| (input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00DE')) {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "UPPER"

	// $ANTLR start "DIGIT"
	public final void mDIGIT() throws RecognitionException {
		try {
			// Tokenizer.g:22:16: ( '0' .. '9' )
			// Tokenizer.g:
			{
				if ((input.LA(1) >= '0' && input.LA(1) <= '9')) {
					input.consume();
					state.failed = false;
				} else {
					if (state.backtracking > 0) {
						state.failed = true;
						return;
					}
					MismatchedSetException mse = new MismatchedSetException(
							null, input);
					recover(mse);
					throw mse;
				}

			}

		} finally {
			// do for sure before leaving
		}
	}

	// $ANTLR end "DIGIT"

	public void mTokens() throws RecognitionException {
		// Tokenizer.g:1:39: ( BLANK | UWRD | LWRD | NUMBER | SYBS | START | END
		// | COL | ROW )
		int alt3 = 9;
		switch (input.LA(1)) {
		case '\t':
		case '\n':
		case '\f':
		case '\r':
		case ' ': {
			alt3 = 1;
		}
			break;
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
		case 'H':
		case 'I':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'N':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'T':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
		case '\u00C0':
		case '\u00C1':
		case '\u00C2':
		case '\u00C3':
		case '\u00C4':
		case '\u00C5':
		case '\u00C6':
		case '\u00C7':
		case '\u00C8':
		case '\u00C9':
		case '\u00CA':
		case '\u00CB':
		case '\u00CC':
		case '\u00CD':
		case '\u00CE':
		case '\u00CF':
		case '\u00D0':
		case '\u00D1':
		case '\u00D2':
		case '\u00D3':
		case '\u00D4':
		case '\u00D5':
		case '\u00D6':
		case '\u00D8':
		case '\u00D9':
		case '\u00DA':
		case '\u00DB':
		case '\u00DC':
		case '\u00DD':
		case '\u00DE': {
			alt3 = 2;
		}
			break;
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'g':
		case 'h':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'n':
		case 'o':
		case 'p':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
		case '\u00DF':
		case '\u00E0':
		case '\u00E1':
		case '\u00E2':
		case '\u00E3':
		case '\u00E4':
		case '\u00E5':
		case '\u00E6':
		case '\u00E7':
		case '\u00E8':
		case '\u00E9':
		case '\u00EA':
		case '\u00EB':
		case '\u00EC':
		case '\u00ED':
		case '\u00EE':
		case '\u00EF':
		case '\u00F0':
		case '\u00F1':
		case '\u00F2':
		case '\u00F3':
		case '\u00F4':
		case '\u00F5':
		case '\u00F6':
		case '\u00F8':
		case '\u00F9':
		case '\u00FA':
		case '\u00FB':
		case '\u00FC':
		case '\u00FD':
		case '\u00FE':
		case '\u00FF': {
			alt3 = 3;
		}
			break;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': {
			alt3 = 4;
		}
			break;
		case '<': {
			int LA3_5 = input.LA(2);

			if ((LA3_5 == '_')) {
				int LA3_7 = input.LA(3);

				if ((LA3_7 == 'S')) {
					alt3 = 6;
				} else if ((LA3_7 == 'E')) {
					alt3 = 7;
				} else {
					NoViableAltException nvae = new NoViableAltException("", 3,
							7, input);

					throw nvae;

				}
			} else {
				alt3 = 5;
			}
		}
			break;
		case '!':
		case '\"':
		case '#':
		case '$':
		case '%':
		case '&':
		case '\'':
		case '(':
		case ')':
		case '*':
		case '+':
		case ',':
		case '-':
		case '.':
		case '/':
		case ':':
		case ';':
		case '=':
		case '>':
		case '?':
		case '@':
		case '[':
		case '\\':
		case ']':
		case '^':
		case '_':
		case '`':
		case '{':
		case '|':
		case '}':
		case '~':
		case '€': {
			alt3 = 5;
		}
			break;
		default:
			if (state.backtracking > 0) {
				state.failed = true;
				return;
			}
			NoViableAltException nvae = new NoViableAltException("", 3, 0,
					input);

			throw nvae;

		}

		switch (alt3) {
		case 1:
		// Tokenizer.g:1:41: BLANK
		{
			mBLANK();
			if (state.failed)
				return;

		}
			break;
		case 2:
		// Tokenizer.g:1:47: UWRD
		{
			mUWRD();
			if (state.failed)
				return;

		}
			break;
		case 3:
		// Tokenizer.g:1:52: LWRD
		{
			mLWRD();
			if (state.failed)
				return;

		}
			break;
		case 4:
		// Tokenizer.g:1:57: NUMBER
		{
			mNUMBER();
			if (state.failed)
				return;

		}
			break;
		case 5:
		// Tokenizer.g:1:64: SYBS
		{
			mSYBS();
			if (state.failed)
				return;

		}
			break;
		case 6:
		// Tokenizer.g:1:69: START
		{
			mSTART();
			if (state.failed)
				return;

		}
			break;
		case 7:
		// Tokenizer.g:1:75: END
		{
			mEND();
			if (state.failed)
				return;

		}
			break;
		case 8:
		// Tokenizer.g:1:79: COL
		{
			mCOL();
			if (state.failed)
				return;

		}
			break;
		case 9:
		// Tokenizer.g:1:83: ROW
		{
			mROW();
			if (state.failed)
				return;

		}
			break;

		}

	}

	// $ANTLR start synpred5_Tokenizer
	public final void synpred5_Tokenizer_fragment() throws RecognitionException {
		// Tokenizer.g:1:64: ( SYBS )
		// Tokenizer.g:
		{
			if ((input.LA(1) >= '!' && input.LA(1) <= '/')
					|| (input.LA(1) >= ':' && input.LA(1) <= '@')
					|| (input.LA(1) >= '[' && input.LA(1) <= '`')
					|| (input.LA(1) >= '{' && input.LA(1) <= '~')
					|| input.LA(1) == '€') {
				input.consume();
				state.failed = false;
			} else {
				if (state.backtracking > 0) {
					state.failed = true;
					return;
				}
				MismatchedSetException mse = new MismatchedSetException(null,
						input);
				recover(mse);
				throw mse;
			}

		}

	}

	// $ANTLR end synpred5_Tokenizer

	// $ANTLR start synpred6_Tokenizer
	public final void synpred6_Tokenizer_fragment() throws RecognitionException {
		// Tokenizer.g:1:69: ( START )
		// Tokenizer.g:1:69: START
		{
			mSTART();
			if (state.failed)
				return;

		}

	}

	// $ANTLR end synpred6_Tokenizer

	// $ANTLR start synpred7_Tokenizer
	public final void synpred7_Tokenizer_fragment() throws RecognitionException {
		// Tokenizer.g:1:75: ( END )
		// Tokenizer.g:1:75: END
		{
			mEND();
			if (state.failed)
				return;

		}

	}

	// $ANTLR end synpred7_Tokenizer

	// $ANTLR start synpred8_Tokenizer
	public final void synpred8_Tokenizer_fragment() throws RecognitionException {
		// Tokenizer.g:1:79: ( COL )
		// Tokenizer.g:1:79: COL
		{
			mCOL();
			if (state.failed)
				return;

		}

	}

	// $ANTLR end synpred8_Tokenizer

	public final boolean synpred8_Tokenizer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred8_Tokenizer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: " + re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed = false;
		return success;
	}

	public final boolean synpred5_Tokenizer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred5_Tokenizer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: " + re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed = false;
		return success;
	}

	public final boolean synpred7_Tokenizer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred7_Tokenizer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: " + re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed = false;
		return success;
	}

	public final boolean synpred6_Tokenizer() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred6_Tokenizer_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: " + re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed = false;
		return success;
	}

}