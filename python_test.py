import broadlink
import os
from time import sleep, time

device = broadlink.hello('192.168.1.11')
if device:
    print("Hello device found via broadcast discovery")
    print(f"Device found: {device}")
    print(f"IP Address: {device.host[0]}")
    print(f"MAC Address: {device.mac.hex()}")
    device.auth()
    # device.enter_learning()
    # # wait for 5 seconds
    # sleep(5)
    # packet = device.check_data()
    # formatted_ints = list(packet)
    # print(f"Device codes: {packet}")
    # print(f"Packet array: {formatted_ints}")
    
    # if packet:  
    #     formatted_ints = list(packet)
        
    #     device.send_data(packet)  
    # else:
    #     print("No data received. Make sure an IR signal was learned.")
        
    ir_dict = {}
    all_ir = []
    base_dir = "saved_ir_codes"
    
    while True:
        user_input = input(
            "\nInput\n1 for learning code \n2 for sending code\n3 for sending just learnt packet"
            "\n4 for learning just packet\n5 for sending all irs\n6 for saving all irs\n7 for loading IR\n"
            "e for exit \ninput: "
        )

        if user_input == "e":
            break

        elif user_input == "1":
            key = input("Input name of the button: ")
            device.enter_learning()
            sleep(5)
            packet = device.check_data()
            all_ir.append(packet)
            ir_dict[key] = packet
            formatted_ints = list(packet)
            print(f"Packet array: {formatted_ints}")

        elif user_input == "2":
            while True:
                key = input("Input name of the button or e: ")
                if key == "e":
                    break
                try:
                    device.send_data(ir_dict[key])
                except:
                    print("Button not learned!")

        elif user_input == "3":
            device.send_data(packet)

        elif user_input == "4":
            device.enter_learning()
            sleep(5)
            packet = device.check_data()
            all_ir.append(packet)
            formatted_ints = list(packet)
            print(f"Packet array: {formatted_ints}")
            device.send_data(packet)
            print("Sent code")
            sleep(1)

        elif user_input == "5":
            for i, ir in enumerate(all_ir):
                print(i)
                device.send_data(ir)
                sleep(0.5)
        elif user_input == "6":
            for i, ir in enumerate(all_ir):
                ir_folder = os.path.join(base_dir, f"ir_{i}")
                os.makedirs(ir_folder, exist_ok=True)
                file_path = os.path.join(ir_folder, "ir_code.bin")

                with open(file_path, "wb") as f:
                    f.write(ir)

            print("All IR codes saved successfully.")

        elif user_input == "7":
            index = input("Enter the IR index to load (e.g., 3 for saved_ir_codes/ir_3/ir_code.bin): ")
            file_path = os.path.join(base_dir, f"ir_{index}", "ir_code.bin")

            if os.path.exists(file_path):
                with open(file_path, "rb") as f:
                    packet = f.read()
                    all_ir.append(packet)
                    ir_dict[index] = packet
                    print(f"Loaded IR from {file_path}")
            else:
                print("File not found!")
                
            print(ir_dict)
        
        elif user_input == "loop":
            file_path = os.path.join(base_dir, f"ir_{0}", "ir_code.bin")

            while True:
                user_input_2 = input("0 for file 0, 1 for file 1, e for exit: ")
                if user_input_2 == "e":
                    break
                elif user_input_2 == "1":                    
                    file_path = os.path.join(base_dir, f"ir_{1}", "ir_code.bin")
                elif user_input_2 == "0":                    
                    file_path = os.path.join(base_dir, f"ir_{0}", "ir_code.bin")
                else:
                    continue
                if os.path.exists(file_path):
                    with open(file_path, "rb") as f:
                        packet = f.read()
                        print(f"Loaded IR from {file_path}")
                    
                print("Sending packet")
                start_time = time()
                device.send_data(packet)
                end_time = time()
                print("time:", end_time-start_time)
                device.send_data(packet)
                formatted_ints = list(packet)
                print(f"Packet array: {formatted_ints}")

                
    
# devices = broadlink.discover(timeout=5, local_ip_address='192.168.1.9')

# if devices:
#     for device in devices:
#         device.auth()
#         print(f"Device found: {device}")
#         print(f"IP Address: {device.host[0]}")
#         print(f"MAC Address: {device.mac.hex()}")
#         device.enter_learning()
#         # wait for 3 seconds
#         sleep(3)
#         packet = device.check_data()
#         print(f"Device codes: {packet}")
#         device.send_data(packet)
        
# else:
#     print("No devices found via broadcast discovery. Trying unicast discovery...")
