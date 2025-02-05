import broadlink
from time import sleep

# device = broadlink.hello('192.168.1.3')
# if device:
#     print("Hello device found via broadcast discovery")
#     print(f"Device found: {device}")
#     print(f"IP Address: {device.host[0]}")
#     print(f"MAC Address: {device.mac.hex()}")
#     device.enter_learning()
#     # wait for 3 seconds
#     sleep(3)
#     packet = device.check_data()
#     print(f"Device codes: {packet}")

devices = broadlink.discover(timeout=5, local_ip_address='192.168.1.9')

if devices:
    for device in devices:
        device.auth()
        print(f"Device found: {device}")
        print(f"IP Address: {device.host[0]}")
        print(f"MAC Address: {device.mac.hex()}")
        device.enter_learning()
        # wait for 3 seconds
        sleep(3)
        packet = device.check_data()
        print(f"Device codes: {packet}")
        device.send_data(packet)
        
else:
    print("No devices found via broadcast discovery. Trying unicast discovery...")
