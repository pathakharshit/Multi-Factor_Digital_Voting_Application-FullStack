from flask import Flask, request, jsonify
import pytesseract
from PIL import Image
import re
from datetime import datetime
import random
import sys

import face_recognition
import numpy as np
from PIL import Image

app = Flask(__name__)

@app.route('/aadhaar_ocr', methods=['POST'])
def aadhaar_ocr():
    """
    Accepts:
      - aadhaarFile: The Aadhaar card image (multipart/form-data)
      - mobileNumber: The user's mobile number (text)
    """
    # 1) Check for the file and mobileNumber
    if 'aadhaarFile' not in request.files:
        return jsonify({'error': 'No Aadhaar file uploaded (aadhaarFile).'}), 400

    mobile_number = request.form.get('mobileNumber')
    if not mobile_number:
        return jsonify({'error': 'No mobileNumber provided.'}), 400

    aadhaar_file = request.files['aadhaarFile']

    try:
        # 2) Perform OCR on the Aadhaar image
        image = Image.open(aadhaar_file.stream)
        text = pytesseract.image_to_string(image)

        # Debug: Print raw OCR text to console
        print("DEBUG OCR TEXT:\n", text, file=sys.stderr)

        # 3) Parse Aadhaar details
        details = parse_aadhaar_text(text)

        # 4) Check if we extracted enough info
        missing_fields = []
        for field in ("aadhaarNumber", "dob", "name"):
            if field not in details or not details[field]:
                missing_fields.append(field)
        if missing_fields:
            return jsonify({
                'error': f"Could not extract all necessary details: {missing_fields}. "
                         f"OCR text might not match the expected format."
            }), 400

        # 5) Calculate age from DOB
        details['age'] = calculate_age(details['dob'])

        # 6) Generate a 4-digit OTP and (simulate) send to mobileNumber
        otp = generate_otp()
        print(f"Sending OTP {otp} to mobile {mobile_number} (simulated)", file=sys.stderr)

        # 7) Return JSON with all details plus OTP
        return jsonify({
            'aadhaarNumber': details.get('aadhaarNumber'),
            'name': details.get('name'),
            'dob': details.get('dob'),
            'age': details.get('age'),
            'otp': otp
        }), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


def parse_aadhaar_text(ocr_text):
    """
    Attempt to parse:
      - Aadhaar number (12 digits, possibly spaced)
      - DOB
      - Name (multi-heuristic approach)
    """
    details = {}

    # 1) Aadhaar Number (12 digits, possibly with spaces)
    #    Example: "8218 9550 3363" -> "821895503363"
    aadhaar_match = re.search(r"\b(\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d[\s]*\d)\b", ocr_text)
    if aadhaar_match:
        raw_aadhaar = aadhaar_match.group(1)
        clean_aadhaar = re.sub(r"\s+", "", raw_aadhaar)  # remove all spaces
        if len(clean_aadhaar) == 12 and clean_aadhaar.isdigit():
            details["aadhaarNumber"] = clean_aadhaar

    # 2) DOB: e.g. "DOB : 07/10/2001" or "DOB: 07-10-2001"
    dob_match = re.search(r"DOB\s*[:\-]?\s*(\d{1,2}[/-]\d{1,2}[/-]\d{4})", ocr_text)
    if dob_match:
        details["dob"] = dob_match.group(1).strip()

    # 3) Name: multi-heuristic approach
    details["name"] = guess_name(ocr_text)

    return details


def guess_name(ocr_text):
    """
    Try to find the name by scanning lines that:
      - Do not contain digits
      - Do not contain skip keywords (DOB, Male, Govt, etc.)
      - Have at least 2 words
      - We'll rank lines by a combined score:
         * start_uppercase_score(line): fraction of words whose first letter is uppercase
         * alpha_ratio(line): fraction of characters that are letters
    We'll pick the line with the highest combined score.
    """
    skip_keywords = [
        "DOB", "Male", "Female", "Govt", "Government", "India", "Address",
        "Aadhaar", "सरकार", "पता", "Signature", "Issue Date", "Card", "Gender",
        "Date of Birth", "W/O", "D/O", "C/O"
    ]

    lines = [line.strip() for line in ocr_text.splitlines() if line.strip()]
    candidate_names = []

    for line in lines:
        # Skip if it contains any skip keyword (case-insensitive)
        if any(k.lower() in line.lower() for k in skip_keywords):
            continue
        # Skip if line has digits
        if re.search(r"\d", line):
            continue
        # Must have at least 2 words
        words = line.split()
        if len(words) < 2:
            continue

        # Compute a combined score
        uppercase_first = start_uppercase_score(line)
        alpha_ratio_val = alpha_ratio(line)
        # Weighted sum or average
        # Emphasize start_uppercase_score more if you prefer
        score = (uppercase_first + alpha_ratio_val) / 2.0

        candidate_names.append((line, score))

    if not candidate_names:
        return None

    # Pick the line with the highest score
    best_line, best_score = max(candidate_names, key=lambda x: x[1])
    return best_line


def start_uppercase_score(line):
    """
    Returns the fraction of words whose first letter is uppercase.
    Example: "Harshit Pathak" -> 2 words, both uppercase first letters => score 1.0
             "eSid Wom" -> 2 words, first letters: 'e', 'W' => score 0.5
    """
    words = line.split()
    if not words:
        return 0
    count = sum(1 for w in words if w[0].isupper())
    return count / len(words)


def alpha_ratio(line):
    """
    Returns the fraction of characters that are alphabetic (A-Z or a-z).
    Higher means it's more likely a name (less punctuation or random symbols).
    """
    alpha_count = sum(ch.isalpha() for ch in line)
    total_count = len(line)
    if total_count == 0:
        return 0
    return alpha_count / total_count


def calculate_age(dob_str):
    """Parse dd/mm/yyyy or dd-mm-yyyy, compute age in years."""
    dob_str = dob_str.replace('-', '/')
    try:
        dob = datetime.strptime(dob_str, "%d/%m/%Y")
    except ValueError:
        return None
    today = datetime.today()
    age = today.year - dob.year
    if (today.month, today.day) < (dob.month, dob.day):
        age -= 1
    return age


def generate_otp():
    return str(random.randint(1000, 9999))


########################################################
# New Face Recognition Endpoints
########################################################

@app.route('/encode_face', methods=['POST'])
def encode_face():
    if 'faceFile' not in request.files:
        return jsonify({'error': 'No faceFile uploaded'}), 400

    file = request.files['faceFile']
    try:
        image = Image.open(file.stream).convert('RGB')
        image_np = np.array(image)
        encodings = face_recognition.face_encodings(image_np)
        if len(encodings) == 0:
            return jsonify({'error': 'No face detected'}), 400
        face_encoding = encodings[0].tolist()
        return jsonify({'encoding': face_encoding}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/compare_faces', methods=['POST'])
def compare_faces():
    if 'faceFile' not in request.files:
        return jsonify({'error': 'No faceFile uploaded'}), 400

    stored_encoding_json = request.form.get('storedEncoding')
    if not stored_encoding_json:
        return jsonify({'error': 'No storedEncoding provided'}), 400

    try:
        stored_encoding = np.array(eval(stored_encoding_json))

        file = request.files['faceFile']
        image = Image.open(file.stream).convert('RGB')
        image_np = np.array(image)
        new_encodings = face_recognition.face_encodings(image_np)
        if len(new_encodings) == 0:
            return jsonify({'match': False, 'error': 'No face detected'}), 200
        new_encoding = new_encodings[0]

        # Compare with a default tolerance of 0.6
        results = face_recognition.compare_faces([stored_encoding], new_encoding, tolerance=0.6)
        match = bool(results[0])

        return jsonify({'match': match}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500




if __name__ == '__main__':
    app.run(port=5000)
