package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

data class PingResult(
    val latencyMs: Int,
    val motd: String = "",
    val players: String = "",
    val version: String = "",
    val isSuccess: Boolean = latencyMs >= 0
)

object RakNetPing {
    // Magic sequence required by Bedrock RakNet protocol
    private val MAGIC = intArrayOf(
        0x00, 0xff, 0xff, 0x00, 0xfe, 0xfe, 0xfe, 0xfe,
        0xfd, 0xfd, 0xfd, 0xfd, 0x12, 0x34, 0x56, 0x78
    )

    suspend fun pingServer(host: String, port: Int = 19132, timeoutMs: Int = 2000): PingResult =
        withContext(Dispatchers.IO) {
            try {
                DatagramSocket().use { socket ->
                    socket.soTimeout = timeoutMs
                    val buf = ByteBuffer.allocate(33)
                    buf.put(0x01.toByte()) // ID_UNCONNECTED_PING
                    buf.putLong(System.currentTimeMillis())
                    for (b in MAGIC) buf.put(b.toByte())
                    buf.putLong(0x4D434D4F42494CL) // "MCMOBIL" client GUID

                    val addr = InetAddress.getByName(host.trim())
                    val t0 = System.nanoTime()
                    val reqPacket = DatagramPacket(buf.array(), buf.position(), addr, port)
                    socket.send(reqPacket)

                    val rxBuf = ByteArray(2048)
                    val rxPacket = DatagramPacket(rxBuf, rxBuf.size)
                    socket.receive(rxPacket)

                    val latency = ((System.nanoTime() - t0) / 1_000_000L).toInt()

                    if (rxPacket.length > 0 && rxPacket.data[0] == 0x1c.toByte()) { // ID_UNCONNECTED_PONG
                        // Parse MOTD string from pong packet: [ID(1 byte)][Time(8 bytes)][GUID(8 bytes)][Magic(16 bytes)][String length(2 bytes)][String...]
                        var motd = ""
                        var players = ""
                        var ver = ""
                        try {
                            if (rxPacket.length > 35) {
                                val strLen = ((rxPacket.data[33].toInt() and 0xFF) shl 8) or
                                        (rxPacket.data[34].toInt() and 0xFF)
                                val payloadLen = minOf(strLen, rxPacket.length - 35)
                                if (payloadLen > 0) {
                                    val str = String(rxPacket.data, 35, payloadLen, Charsets.UTF_8)
                                    // Bedrock format: Edition;MOTD_1;Protocol;Version;Players;MaxPlayers;ServerID;MOTD_2;GameMode
                                    val parts = str.split(";")
                                    if (parts.size >= 2) motd = parts[1].replace(Regex("§[0-9a-fk-or]"), "")
                                    if (parts.size >= 4) ver = parts[3]
                                    if (parts.size >= 6) players = "${parts[4]}/${parts[5]}"
                                }
                            }
                        } catch (_: Exception) { }

                        PingResult(
                            latencyMs = latency,
                            motd = motd.ifBlank { "Bedrock Server Online" },
                            players = players,
                            version = ver,
                            isSuccess = true
                        )
                    } else {
                        // Responded with different packet
                        PingResult(latencyMs = latency, motd = "Online", isSuccess = true)
                    }
                }
            } catch (e: Exception) {
                // If UDP fails or blocked in sandbox, return failure
                PingResult(latencyMs = -1, motd = "Offline or unreachable", isSuccess = false)
            }
        }
}
