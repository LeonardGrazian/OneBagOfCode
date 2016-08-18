import os

def has_file_ext(file_name, file_ext):
	file_name_parts = file_name.split(".")
	return file_name_parts[-1].lower() == file_ext

def is_target_file(file_name):
	target_extensions = ["java", "cpp", "hpp", "h"]
	for extension in target_extensions:
		if has_file_ext(file_name, extension):
			return True
	return False

def has_string(file_path, search_string):
	try:
		file_path = file_path.lower()
		file_object = open(file_path, "r")
		for line in file_object:
			line_lower = line.lower()
			if search_string in line_lower:
				return True
		return False
	except:
		return False

start_path = "C:\\OpenCV\\opencv-source-1\\opencv\platforms\\android-arm\\install\\sdk\\java"
search_string = "imdecode"#"makeptr" #"org.j3d.loaders.stl"
matches = []

for root, dirs, files in os.walk(start_path):	
	for f in files:
		if is_target_file(f):
			print ( "Checking file " + f )
			file_path = "\\".join((root, f))
			if has_string(file_path, search_string):
				matches.append(file_path)

if len(matches) > 0:
	print ( "MATCHES:" )
	for match in matches:
		print ( match )
else:
	print ( "NO MATCHES" )