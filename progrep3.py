from scipy import stats

# Execution times
single_machine_data = [330.7639, 320.9739, 199.8915, 108.0370, 81.8835, 76.2193, 73.0420, 69.9831, 69.3408, 68.2525, 68.1374]
distributed_data = [263.3898, 256.2561, 138.7919, 84.0363, 74.4635, 63.5734, 60.1425, 59.3503, 57.5136, 57.6334, 56.7955]

t_statistic, p_value = stats.ttest_rel(single_machine_data, distributed_data)

print("Paired t-test results:")
print("t-statistic:", t_statistic)
print("p-value:", p_value)

# Check the significance level
alpha = 0.05
if p_value < alpha:
    print("Reject the null hypothesis: There is a significant difference in the mean runtime performance.")
else:
    print("Fail to reject the null hypothesis: There is no significant difference in mean runtime performance.")