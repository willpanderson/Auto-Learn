"""
    AutoLearn Project
    CSE 3310-004
    TEAM 2 :: Ryan Laurents - Edrik Aguilera - William Anderson
"""

import tensorflow as tf

from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Conv2D, Flatten, Dropout, MaxPooling2D
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import os
import numpy as np
import matplotlib.pyplot as plt

# set batch size/epoch count/image size
batch_size = 128
epochs = 10
IMG_HEIGHT = 185
IMG_WIDTH = 259

# create a function to get the label for a particular car
def get_label(file_path):
    # convert path to list of components
    parts = tf.strings.split(file_path, os.path.sep)
    # the second part is the model_id
    model_id = parts[2]
    f_label = open("C:/AutoLearn/Vehicle Data Set/data/data/misc/attributes.txt") # contains all of the image attributes sorted by model_id
    for line in f_label:
        values = line.split()
        if (values[0] == model_id): # the first value in each line is the model_id
            return values[5] # classification label is at the end of each line in the 6th position
    f_label.close()

# fucntion to retrieve and return a resized image
    
def decode_img(img):
    # convert the compressed string to a 3D uint8 tensor
    img = tf.image.decode_jpeg(img, channels=3)
    # Use `convert_image_dtype` to convert to floats in the [0,1] range.
    img = tf.image.convert_image_dtype(img, tf.float32)
    # resize the image to the desired size.
    return tf.image.resize(img, [IMG_WIDTH, IMG_HEIGHT])

# function to process both the image and label from the file path
def process_path(file_path):
    label = get_label(file_path)
    # load the raw data from the file as a string
    img = tf.io.read_file(file_path)
    img = decode_img(img)
    return img, label

   
train_file = 'C:/AutoLearn/Vehicle Data Set/data/data/train_test_split/classification/train.txt'
test_file = 'C:/AutoLearn/Vehicle Data Set/data/data/train_test_split/classification/test.txt'
train_images_array = []
test_images_array = []

# move file locations to array
with open(train_file) as my_file_train:
    for line in my_file_train:
        train_images_array.append(line)
        
with open(test_file) as my_file_test:
    for line in my_file_test:
        test_images_array.append(line)
    
# transform into a tensorflow dataset object
train_ds = tf.data.Dataset.list_files(train_images_array)
test_ds = tf.data.Dataset.list_files(test_images_array)




# transform into a dataset map with labels included
# set parallel calls so multiple images are loaded/processed in parallel

labeled_training_ds = train_ds.map(process_path) 
labeled_testing_ds = test_ds.map(process_path)


# Generators preprocess images into batches of tensors
train_image_generator = ImageDataGenerator(rescale = 1./255)
validation_image_generator = ImageDataGenerator(rescale = 1./255)

# Model Convolutions/pooling
model = Sequential([
    Conv2D(16, 3, padding='same', activation='relu', input_shape=(IMG_HEIGHT, IMG_WIDTH ,3)),
    MaxPooling2D(),
    Conv2D(32, 3, padding='same', activation='relu'),
    MaxPooling2D(),
    Conv2D(64, 3, padding='same', activation='relu'),
    MaxPooling2D(),
    Flatten(),
    Dense(512, activation='relu'),
    Dense(1)
])

# Compile model
model.compile(optimizer='adam', loss=tf.keras.losses.BinaryCrossentropy(from_logits=True), metrics=['accuracy'])
model.summary()

"""
history = model.fit_generator(
    train_image_generator,
    steps_per_epoch= batch_size // batch_size,
    epochs=epochs,
    validation_data=validation_image_generator,
    validation_steps= batch_size// batch_size
)
"""
















