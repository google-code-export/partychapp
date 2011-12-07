// -----------------------------------------------------------------------
// pion-common: a collection of common libraries used by the Pion Platform
// -----------------------------------------------------------------------
// Copyright (C) 2007-2011 Atomic Labs, Inc.  (http://www.atomiclabs.com)
//
// Distributed under the Boost Software License, Version 1.0.
// See http://www.boost.org/LICENSE_1_0.txt
//

#ifndef __PION_ALGORITHMS_HEADER__
#define __PION_ALGORITHMS_HEADER__

#include <string>
#include <pion/PionConfig.hpp>


namespace pion {	// begin namespace pion

struct PION_COMMON_API algo {

	/** base64 decoding
	 *
	 * @param input - base64 encoded string
	 * @param output - decoded string ( may include non-text chars)
	 * @return true if successful, false if input string contains non-base64 symbols
	 */
	static bool base64_decode(std::string const &input, std::string & output);

	/** base64 encoding
	 *
	 * @param input - arbitrary string ( may include non-text chars)
	 * @param output - base64 encoded string
	 * @return true if successful,
	 */
	static bool base64_encode(std::string const &input, std::string & output);

	/// escapes URL-encoded strings (a%20value+with%20spaces)
	static std::string url_decode(const std::string& str);

	/// encodes strings so that they are safe for URLs (with%20spaces)
	static std::string url_encode(const std::string& str);

};
	
}	// end namespace pion

#endif
