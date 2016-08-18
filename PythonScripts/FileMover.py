import os
import shutil


def is_target_dir(dir_name):
	return dir_name[0].lower() == "s"

def extract_subject_id(dir_name):
	return int(dir_name[1:])

def extract_picture_number(pic_name):
	try: 
		return int(pic_name.split(".")[0])
	except:
		return -1

	# prefix_part = pic_name.split(".")[0]
	# prefix_length = prefix_part.length()
	# for i in range(prefix_length):
	# 	try:
	# 		pic_num = int(prefix_part[i:])
	# 		return pic_num
	# 	except:
	# 		pass
	# return -1 # no number in this name

def get_new_name(old_name, subject_id):
	pic_num = extract_picture_number(old_name)
	return "subject_" + str(subject_id) + "_sample_" + str(pic_num) + ".pgm" 


start_path = "C:\\Users\\Mohawk Group\\Downloads\\att_faces"
target_dir = "C:\\Users\\Mohawk Group\\Desktop\\TempFaceFolder"
num_subjects = 40 # number of subjects to transfer
num_existing = 0 # number of subjects already transferred TODO: find more robust solution

for root, dirs, files in os.walk(start_path):	
	for dir_name in dirs:
		if is_target_dir(dir_name):
			subject_id = extract_subject_id(dir_name)
			if subject_id <= num_subjects and subject_id > num_existing:
				path_to_dir = "\\".join([root, dir_name])
				for subject_file_name in os.listdir(path_to_dir):
					new_file_name = get_new_name(subject_file_name, str(subject_id))
					path_to_old_file = "\\".join([path_to_dir, subject_file_name])
					print ( path_to_old_file + " -> " + new_file_name )
					
					# copy subject file to target directory
					path_to_new_file = "\\".join([target_dir, new_file_name])
					shutil.copyfile(path_to_old_file, path_to_new_file)