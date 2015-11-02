import re
import hashlib
from datetime import datetime
from time import mktime, gmtime
from urlparse import urlparse


def getCurrentTime():
    return datetime.today().strftime("%Y-%m-%d %H:%M:%S")


def countryUri(x):
    "Return a URI for a country given its name."
    import re

    x = re.sub('[^A-Za-z0-9]+', '', x)
    return x.lower()


def personNameUri(x):
    "Return a URI for a person name."
    import re

    x = re.sub('[^A-Za-z0-9]+', '', x.strip())
    return x.lower()


def toTitleCaseIfUpper(x):
    "Return the string in title case if it is all upper, otherwise leave capitalization alone."
    x = x.strip()
    if x.isupper():
        return x.title()
    else:
        return x


def nonWhitespace(x):
    "Return the string removing all spaces."
    import re

    y = re.sub(r'\s+', '', x.strip())
    return y


def toTitleCaseCleaned(x):
    "Return the string in title case cleaning spaces."
    import re

    y = re.sub(r'\s+', ' ', x.strip())
    return y.title()


def nonAsciiChars(x):
    "Return a set of the non-ascii chars in x"
    import re

    return set(re.sub('[\x00-\x7f]', '', x))


def nonAsciiCharsAsString(x):
    "Return a string containing a comma-separated list of non-ascii chars in x"
    y = list(nonAsciiChars(x))
    y.sort()
    return ', '.join(y)


def asciiChars(x):
    "Remove non-ascii chars in x replacing consecutive ones with a single space"
    import re

    return re.sub(r'[^\x00-\x7F]+', ' ', x)


def alphaNumeric(x):
    "Replace consecutive non-alphanumeric bya single space"
    return re.sub('[^A-Za-z0-9]+', ' ', x)


def alphaNumericOnly(x):
    "Remove non-alphanumerics"
    return re.sub('[^A-Za-z0-9]+', '', x)


def numericOnly(x):
    "Remove non-numeric chars from the string x"
    return re.sub('[^0-9]+', '', x)


def alphaOnly(x):
    "Remove non-alphabetic chars from the string x"
    return re.sub('[^A-Za-z]+', '', x)


def alphaOnlyPreserveSpace(x):
    x = re.sub('[^A-Za-z\s]+', '', x)
    y = re.sub(r'\s+', ' ', x.strip())
    return y


def isSymbol(char1):
    if char1.isalnum():
        return False
    return True


def fingerprintString(x):
    "Make a fingerprint like the one google refine makes"
    x = alphaNumeric(asciiChars(x)).lower()
    y = list(set(x.split()))
    y.sort()
    return '_'.join(y)


def mungeForUri(x):
    "Take an arbitrary string and make it into something that can be used as a URI"
    return alphaNumericOnly(asciiChars(x)).lower()


def md5Hash(x):
    "Return md5 hash of x"
    import hashlib

    return hashlib.md5(x).hexdigest()


def tenDigitPhoneNumber(x):
    """Return the 10-digit phone number of a phone, as 10 consecutive digits"""
    return re.sub('[^0-9]+', '', x)


def iso8601date(date, format=None):
    """Convert a date to ISO8601 date format

input format: YYYY-MM-DD HH:MM:SS GMT (works less reliably for other TZs)
or            YYYY-MM-DD HH:MM:SS.0
or            YYYY-MM-DD
or            epoch (13 digit, indicating ms)
or            epoch (10 digit, indicating sec)
output format: iso8601

"""

    try:
        return datetime.strptime(date, "%Y-%m-%d %H:%M:%S %Z").isoformat()
    except Exception:
        pass

    if format:
        try:
            return datetime.strptime(date, format).isoformat()
        except Exception:
            pass

    try:
        return datetime.strptime(date, "%Y-%m-%d %H:%M:%S.0").isoformat()
    except:
        pass

    try:
        return datetime.strptime(date, "%Y-%m-%d").isoformat()
    except:
        pass

    try:
        date = int(date)
        if 1000000000000 < date and date < 9999999999999:
            # 13 digit epoch
            return datetime.fromtimestamp(mktime(gmtime(date / 1000))).isoformat()
    except:
        pass

    try:
        date = int(date)
        if 1000000000 < date and date < 9999999999:
            # 10 digit epoch
            return datetime.fromtimestamp(mktime(gmtime(date))).isoformat()
    except:
        pass
    # If all else fails, return input
    return ''


def getYearFromISODate(isoDate):
    if isoDate:
        return isoDate[0:4]
    return ''


def getWebsiteDomain(url):
    parsed_uri = urlparse(url)
    if parsed_uri:
        domain = parsed_uri.netloc
        if domain:
            if domain.startswith("www."):
                domain = domain[4:]
            return domain
    return ''


def getTextHash(text):
    if text:
        return hashlib.sha1(text.encode('utf-8')).hexdigest()
    return ''