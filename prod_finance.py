'''
Created on Apr 30, 2018

@author: Dale Angus (daleangus@hotmail.com)
'''
import math
from sklearn.cross_validation import train_test_split
from dnn_app_utils_v3 import *
import matplotlib.pyplot as plt
import pandas as pd
import pickle
import time


def loadproddata():
    rawdata = pd.read_csv("dataset/rawdataprod0518.csv")
    #print(rawdata.info)
    
    #rawdata2 = pd.read_csv("dataset/rawdataprod0521.csv")
    
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
    traindata = rawdata[['pctchange', 'slope10', 'slope21', 'awesomeoscillator', 'momentum34', 'tangentslope', 'open', 'high', 'low', 'weighting', 'vixclose', 'vixpctchange', 'sector']]
    traindata['sector'] = traindata['sector'].apply(lambda x: sector_dictionary[x])
    labeldata = rawdata[['spyupdown']]
        
    fill_value = pd.DataFrame({col: traindata.mean(axis=1) for col in traindata.columns})
    traindata.fillna(fill_value, inplace=True)
    print(traindata.describe)

    days = 0
    traindata = traindata[0:traindata.shape[0] - 1 - days]
    labeldata = labeldata[days:labeldata.shape[0] - 1]
    return traindata, labeldata
    

# Starts here    
#load trained parameters
with open('tf_sigmoid_nn-20180530-194013.pickle', 'rb') as handle:
    parameters = pickle.load(handle)

print(parameters)
prod_x_orig, prod_y_orig = loadproddata()  # feeds one day of constituent data
prod_x = np.asmatrix(prod_x_orig)
prod_y = np.asmatrix(prod_y_orig)
print ("Prod X shape: " + str(prod_x.shape))
print ("Prod Y shape: " + str(prod_y.shape))

pred_prod=0
for i in range(prod_x.shape[0]):
    pred_prod= predict(prod_x[i].T, prod_y[i], parameters) 
    print("Production prediction: " + str(i) + ": " + str(int(np.squeeze(pred_prod))))



