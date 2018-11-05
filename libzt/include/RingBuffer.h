/*
 * ZeroTier SDK - Network Virtualization Everywhere
 * Copyright (C) 2011-2018  ZeroTier, Inc.  https://www.zerotier.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * --
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial closed-source software that incorporates or links
 * directly against ZeroTier software without disclosing the source code
 * of your own application.
 */

/**
 * @file
 *
 * Ring buffer implementation for network stack drivers
 */

#ifndef ZT_RINGBUFFER_H
#define ZT_RINGBUFFER_H

#include <cstdint>
#include <stdlib.h>


typedef char bufElementType;

class RingBuffer
{
private:
	bufElementType * buf;
	size_t size;
	size_t begin;
	size_t end;
	bool wrap;

public:
	/**
	* create a RingBuffer with space for up to size elements.
	*/
	explicit RingBuffer(size_t size)
		: size(size),
		begin(0),
		end(0),
		wrap(false)
	{
		buf = new bufElementType[size];
	}
/*
	RingBuffer(const RingBuffer<T> & ring)
	{
		this(ring.size);
		begin = ring.begin;
		end = ring.end;
		memcpy(buf, ring.buf, sizeof(T) * size);
	}
*/
	~RingBuffer()
	{
		delete[] buf;
	}

	// get a reference to the underlying buffer
	bufElementType* get_buf();

	// adjust buffer index pointer as if we copied data in
	size_t produce(size_t n);

	// merely reset the buffer pointer, doesn't erase contents
	void reset();

	// adjust buffer index pointer as if we copied data out
	size_t consume(size_t n);

	size_t write(const bufElementType * data, size_t n);

	size_t read(bufElementType * dest, size_t n);

	size_t count();

	size_t getFree();
};

#endif // _H
