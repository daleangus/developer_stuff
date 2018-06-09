import pickle
import time

from sklearn.cross_validation import train_test_split
from tensorflow.python.framework import ops

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import tensorflow as tf
from tf_utils import (random_mini_batches, predict)


def loadproddata(prod_filename):
    rawdata = pd.read_csv(prod_filename)
    print(rawdata.info)
    
    sector_dictionary = {'Basic Materials':1,
'Capital Goods':2,
'Conglomerates':3,
'Consumer Cyclical':4,
'Consumer/Non-Cyclical':5,
'Energy':6,
'Financial':7,
'Healthcare':8,
'Services':9,
'Technology':10,
'Transportation':11,
'Utilities':12 }

    #proddata = rawdata[['pctchange', 'slope10', 'slope21', 'awesomeoscillator', 'momentum34', 'tangentslope', 'open', 'high', 'low', 'weighting', 'vixclose', 'vixpctchange', 'sector', 'diapctchange', 'iwmpctchange', 'oneqpctchange']]
    proddata = rawdata[['pctchange', 'slope10', 'slope21', 'awesomeoscillator', 'momentum34', 'tangentslope', 'open', 'high', 'low', 'weighting', 'vixclose', 'vixpctchange', 'sector']]
    proddata['sector'] = proddata['sector'].apply(lambda x: sector_dictionary[x])
    labeldata = rawdata[['dn3', 'dn2', 'dn1', 'dn0', 'up0', 'up1', 'up2', 'up3']]
        
    fill_value = pd.DataFrame({col: proddata.mean(axis=1) for col in proddata.columns})
    proddata.fillna(fill_value, inplace=True)
    print(proddata.describe)

    return proddata, labeldata


#load trained parameters
layer_dims=[13, 25, 12, 8]
prod_filename = "dataset/rawdataprod0523.csv"
ground_truth=3

pickle_name = 'tf_softmax_nn-20180530-194327.pickle'  
with open(pickle_name, 'rb') as handle:
    parameters = pickle.load(handle)

print(parameters)
prod_x_orig, prod_y_orig = loadproddata()  # feeds one day of constituent data
prod_x = np.asmatrix(prod_x_orig)
prod_y = np.asmatrix(prod_y_orig)
print ("Prod X shape: " + str(prod_x.shape))
print ("Prod Y shape: " + str(prod_y.shape))

correct = 0
for i in range(prod_x.shape[0]):
    pred_prod = predict(prod_x[i].T, parameters, layer_dims)
    if pred_prod == ground_truth:
        correct = correct + 1 
    print("Production prediction: " + str(i) + ": " + str(int(np.squeeze(pred_prod))))
ave = correct/prod_x.shape[0] * 100
print("Average Accuracy: " + str(np.squeeze(correct)) + " out of " + str(prod_x.shape[0]) + " or " + str(np.squeeze(ave)) + "%")

