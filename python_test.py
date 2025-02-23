import json
import base64
import os

# Define the source directory where IR code binaries are stored
source_dir = "saved_ir_codes"
output_dir = "ir_codes"
output_file = os.path.join(output_dir, "all_codes.json")

# Ensure output directory exists
os.makedirs(output_dir, exist_ok=True)

# Dictionary to store encoded IR codes
encoded_ir_codes = {}

# Iterate over each folder in saved_ir_codes
for button_name in os.listdir(source_dir):
    button_path = os.path.join(source_dir, button_name, "ir_code.bin")
    
    if os.path.isfile(button_path):
        with open(button_path, "rb") as f:
            binary_data = f.read()
            encoded_ir_codes[button_name[3:]] = base64.b64encode(binary_data).decode("utf-8")

with open(output_file, "w") as f:
    json.dump(encoded_ir_codes, f, indent=4)

print(f"File saved at: {output_file}")