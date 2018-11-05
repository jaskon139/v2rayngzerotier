/* libanode: the Anode C reference implementation
 * Copyright (C) 2009-2010 Adam Ierymenko <adam.ierymenko@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */

#ifndef _ANODE_MUTEX_H
#define _ANODE_MUTEX_H

#ifdef WINDOWS

#else /* WINDOWS */

#include <pthread.h>

#define AnodeMutex pthread_mutex_t
#define AnodeMutex_init(m) pthread_mutex_init((m),(const pthread_mutexattr_t *)0)
#define AnodeMutex_destroy(m) pthread_mutex_destroy((m))
#define AnodeMutex_lock(m) pthread_mutex_lock((m))
#define AnodeMutex_unlock(m) pthread_mutex_unlock((m))

#endif /* WINDOWS */

#endif
