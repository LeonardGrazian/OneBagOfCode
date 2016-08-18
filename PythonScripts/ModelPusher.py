#!/usr/bin/python2.4
#
# Small script to show PostgreSQL and Pyscopg together
#

import sys
import struct
import psycopg2
import random
import array

def is_normal_line(line_elements):
	for element in line_elements:
		if (element.lower() == "normal"):
			return True
	return False

def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

# connect to our database
conn = 0
try:
    conn = psycopg2.connect("dbname='deg0aem4ejd70t' user='wbqlfhrskshcwo' host='ec2-50-19-219-148.compute-1.amazonaws.com' password='QEvBvJ0ZlvKdT866KVdKlNV05n'")
    print ("Successfully connected to the database")
except:
    print ("I am unable to connect to the database")
    sys.exit()

cursor = conn.cursor() # get cursor so we can execute commands

file_name = "utah_teapot" # omit .stl extension
# create table of this name if one does not already exist
cursor.execute( "CREATE TABLE IF NOT EXISTS models (model_name TEXT, stl_data BYTEA);" )


# Open stl file
stl_file_object = open(file_name + ".stl", "r")

model_data = [] # data for the whole stl
normal = []
for line in stl_file_object:
	line_elements = line.split()
	is_vertex_line = False

	if is_normal_line(line_elements):
		normal = []
		for element in line_elements:
			if is_number(element):
				normal.append(float(element))
	else:
		count = 0
		for element in line_elements:
			if is_number(element):
				count += 1
				is_vertex_line = True
				model_data.append(float(element))
			if count == 3:
				count = 0
				model_data.append(1.0)
				model_data.append(normal[0])
				model_data.append(normal[1])
				model_data.append(normal[2])
				model_data.append(0.0)			

model_data_array = array.array("f", model_data) # better to just create array from the outset?

cursor.execute( "INSERT INTO models VALUES (%(name)s, %(bytes)s);", 
				{"name" : file_name, "bytes" : bytes(model_data_array)})

# Load byte data into bytea object and add to database

cursor.execute( "SELECT * FROM models" )

rows = cursor.fetchall()

for r in rows:
	print(r)

conn.commit()
conn.close()