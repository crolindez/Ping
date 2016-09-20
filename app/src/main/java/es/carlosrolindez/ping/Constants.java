/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.carlosrolindez.ping;

import java.util.UUID;

class Constants {

	public static final int MESSAGE = 0x400 + 1;
	public static final int MY_HANDLE = 0x400 + 2;
	public static final int MY_CLOSE = 0x400 + 3;

	public static final String NameService = "es.carlosrolindez.ping.PING";

	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Intent request codes
	public static final int REQUEST_ENABLE_BT = 1;


}
