Given this table

product, category, revenue
bendable, cell phone, 3000
big, tablet, 2500
foldable, cell phone, 3000
mini, tablet, 5500
normal, tablet, 1500
pro, tablet, 4500
pro2, tablet, 6500
thin, cell phone, 6000
ultra thin, cell phone, 5000
very thin, cell phone, 6000

Get the 1st and 2nd most profitable from each category.

Solution:
set @num := 0, @category := '';
select category, product, revenue from 
(select category, product, revenue, @num := if(@category=category, @num+1, 1) as row_number, @category := category as dummy from 
 (select * from products order by category, revenue desc) x
) y where row_number <=2;

Result:
category, product, revenue
cell phone, very thin, 6000
cell phone, thin, 6000
tablet, pro2, 6500
tablet, mini, 5500
