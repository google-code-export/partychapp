/* common/include/pion/PionConfig.hpp.  Generated from PionConfig.hpp.in by configure.  */
// -----------------------------------------------------------------------
// pion-common: a collection of common libraries used by the Pion Platform
// -----------------------------------------------------------------------
// Copyright (C) 2007-2008 Atomic Labs, Inc.  (http://www.atomiclabs.com)
//
// Distributed under the Boost Software License, Version 1.0.
// See accompanying file COPYING or copy at http://www.boost.org/LICENSE_1_0.txt
//

#ifndef __PION_PIONCONFIG_HEADER__
#define __PION_PIONCONFIG_HEADER__

// DO NOT USE autoheader ; this file is not automanaged!!!

/* Define to the version number of pion. */
#define PION_VERSION "4.0.7"

/* Define to the directory where Pion plug-ins are installed. */
#define PION_PLUGINS_DIRECTORY "/usr/local/share/pion/plugins"

/* Define to the directory where cygwin is installed. */
#define PION_CYGWIN_DIRECTORY ""

/* Define to 1 if C library supports malloc_trim() */
#define PION_HAVE_MALLOC_TRIM 1

// -----------------------------------------------------------------------
// hash_map support
//
// At least one of the following options should be defined.

/* Define to 1 if you have the <ext/hash_map> header file. */
/* #undef PION_HAVE_EXT_HASH_MAP */

/* Define to 1 if you have the <hash_map> header file. */
/* #undef PION_HAVE_HASH_MAP */

/* Define to 1 if you have the <unordered_map> header file. */
#define PION_HAVE_UNORDERED_MAP 1

// -----------------------------------------------------------------------
// Logging Options
//
// At most one of the logging options below should be defined.  If none of 
// them are defined, std::cout and std::cerr will be used for logging.

/* Define to 1 to use the `log4cplus' library (-llog4cplus) for logging. */
/* #undef PION_USE_LOG4CPLUS */

/* Define to 1 to use the `log4cxx' library (-llog4cxx) for logging. */
/* #undef PION_USE_LOG4CXX */

/* Define to 1 to use the `log4cpp' library (-llog4cpp) for logging. */
/* #undef PION_USE_LOG4CPP */

/* Define to 1 to disable logging. */
#define PION_DISABLE_LOGGING 1

// -----------------------------------------------------------------------

/* Define to 1 if you have the `zlib' library. */
#define PION_HAVE_ZLIB 1

/* Define to 1 if you have the `bzlib' library. */
#define PION_HAVE_BZLIB 1

/* Define to 1 if you have the `OpenSSL' library. */
#define PION_HAVE_SSL 1

/* Define to 1 if you have the `boost.lockfree' library. */
#define PION_HAVE_LOCKFREE 1


/* The following are only used by the Windows projects */
#define PION_COMMON_API
#define PION_PLUGIN_API
#define PION_NET_API
#define PION_SERVICE_API
#define PION_PLATFORM_API
#define PION_SERVER_API


#endif
