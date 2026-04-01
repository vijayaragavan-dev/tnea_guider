SELECT c.name as college_name, d.name as district, c.tier,
       b.name as branch, cat.name as category,
       cd.cutoff, cd.fees, cd.placement_rate
FROM cutoff_data cd
JOIN colleges c ON cd.college_id = c.id
JOIN districts d ON c.district_id = d.id
JOIN branches b ON cd.branch_id = b.id
JOIN categories cat ON cd.category_id = cat.id
WHERE (cd.cutoff >= ? OR ? IS NULL)
  AND (cd.cutoff <= ? OR ? IS NULL)
  AND (d.name = ? OR ? IS NULL)
  AND (cd.fees <= ? OR ? IS NULL)
  AND (c.tier = ? OR ? IS NULL)
ORDER BY cd.cutoff DESC, cd.placement_rate DESC
LIMIT 20;