import pandas as pd


INPUT_FILE = "final_dataset.csv"
OUTPUT_FILE = "cleaned_dataset_final.csv"


def main() -> None:
    df = pd.read_csv(INPUT_FILE)
    orig_rows = len(df)
    orig = df.copy()

    for col in ["college_name", "district", "tier", "branch", "category"]:
        df[col] = df[col].astype(str).str.replace(r"\s+", " ", regex=True).str.strip()

    name_fixes = {
        "Bethlahem Institute of Engineering": "Bethlehem Institute of Engineering",
        "Mother Terasa College of Engineering and Technology": "Mother Teresa College of Engineering and Technology",
        "Misrimal Navajee munoth Jain Engineering College": "Misrimal Navajee Munoth Jain Engineering College",
        "Vetri Vinayaha College of Engineering and Technology": "Vetri Vinayaga College of Engineering and Technology",
        "St. Joseph College Engineering": "St. Joseph College of Engineering",
        "Vivekanandha College of Technology for women": "Vivekanandha College of Technology for Women",
        "K.C.G. College of Technology(Autonomous)": "K.C.G. College of Technology (Autonomous)",
        "K.S.R. Institute for Engineering and Technology(Autonomous)": "K.S.R. Institute for Engineering and Technology (Autonomous)",
        "N P R College of Engineering and Technology(Autonomous)": "N P R College of Engineering and Technology (Autonomous)",
        "Nehru Institute of Technology(Autonomous)": "Nehru Institute of Technology (Autonomous)",
        "Paavai Engineering College(Autonomous)": "Paavai Engineering College (Autonomous)",
        "Knowledge Institute of Technology(Autonomous)": "Knowledge Institute of Technology (Autonomous)",
        "PSN College of Engineering and Technology(Autonomous)": "PSN College of Engineering and Technology (Autonomous)",
        "M.I.E.T Engineering College": "M.I.E.T. Engineering College",
        "Prathyusha Engineering College(Autonomous)": "Prathyusha Engineering College (Autonomous)",
        "Velalar College of Engineering and Technology(Autonomous)": "Velalar College of Engineering and Technology (Autonomous)",
    }
    df["college_name"] = df["college_name"].replace(name_fixes)

    district_fixes = {
        "A.V.C. College of Engineering": "Mayiladuthurai",
        "Adhiparasakthi College of Engineering": "Chengalpattu",
        "Adhiparasakthi Engineering College": "Kanchipuram",
        "Alagappa Chettiar Government College of Engineering and Technology (Autonomous)": "Sivaganga",
        "Anjalai Ammal Mahalingam Engineering College": "Tiruvarur",
        "Annai College of Engineering and Technology": "Thanjavur",
        "Annai Mathammal Sheela Engineering College": "Namakkal",
        "Arulmigu Meenakshi Amman College of Engineering": "Kanchipuram",
        "C.Abdul Hakeem College of Engineering and Technology": "Ranipet",
        "Dhanalakshmi College of Engineering": "Kanchipuram",
        "Dhanalakshmi Srinivasan College of Engineering and Technology": "Perambalur",
        "Dhanalakshmi Srinivasan Engineering College (Autonomous)": "Perambalur",
        "Dr. Navalar Nedunchezhiyan College of Engineering": "Cuddalore",
        "E.G.S. Pillay Engineering College (Autonomous)": "Nagapattinam",
        "Easwari Engineering College (Autonomous)": "Chennai",
        "Excel Engineering College (Autonomous)": "Namakkal",
        "Ganadipathy Tulsi's Jain Engineering College": "Vellore",
        "Gojan School of Business and Technology": "Tiruvallur",
        "Government College of Engineering - Dharmapuri": "Dharmapuri",
        "Government College of Engineering Bargur (Autonomous)": "Krishnagiri",
        "Government College of Engineering-Thanjavur": "Thanjavur",
        "IFET College of Engineering(Autonomous)": "Villupuram",
        "IFET College of Engineering (Autonomous)": "Villupuram",
        "Indra Ganesan College of Engineering": "Tiruchirappalli",
        "J N N Institute of Engineering": "Tiruvallur",
        "Jaya College of Engineering and Technology": "Tiruvallur",
        "Jaya Engineering College": "Tiruvallur",
        "Jaya Institute of Technology": "Tiruvallur",
        "Jaya Sakthi Engineering College": "Tiruvallur",
        "Jeppiaar Institute of Technology": "Kanchipuram",
        "K. Ramakrishnan College of Engineering (Autonomous)": "Tiruchirappalli",
        "K. Ramakrishnan College of Technology (Autonomous)": "Tiruchirappalli",
        "Karpaga Vinayaga College of Engineering and Technology": "Chengalpattu",
        "Kings Engineering College": "Kanchipuram",
        "Kongunadu College of Engineering and Technology (Autonomous)": "Tiruchirappalli",
        "Loyola Institute of Technology": "Chennai",
        "M.A.M College of Engineering and Technology": "Tiruchirappalli",
        "M.A.M. College of Engineering": "Tiruchirappalli",
        "M.A.M. School of Engineering": "Tiruchirappalli",
        "M.I.E.T. Engineering College": "Tiruchirappalli",
        "Mailam Engineering College": "Villupuram",
        "Madha Engineering College": "Chennai",
        "Madha Institute of Engineering and Technology": "Kanchipuram",
        "Meenakshi Ramaswamy Engineering College": "Ariyalur",
        "Mookambigai College of Engineering": "Pudukkottai",
        "Mother Teresa College of Engineering and Technology": "Pudukkottai",
        "Mount Zion College of Engineering and Technology": "Pudukkottai",
        "N P R College of Engineering and Technology (Autonomous)": "Dindigul",
        "National Engineering College (Autonomous)": "Thoothukudi",
        "Noorul Islam College of Engineering and Technology": "Kanyakumari",
        "OASYS Institute of Technology": "Tiruchirappalli",
        "P.S.V. College of Engineering and Technology": "Krishnagiri",
        "Panimalar Engineering College (Autonomous)": "Chennai",
        "Prathyusha Engineering College (Autonomous)": "Tiruvallur",
        "R.M.D. Engineering College (Autonomous)": "Tiruvallur",
        "R.M.K. College of Engineering and Technology (Autonomous)": "Tiruvallur",
        "RMK Engineering College": "Tiruvallur",
        "Rajalakshmi Engineering College (Autonomous)": "Chennai",
        "Rajalakshmi Institute of Technology(Autonomous)": "Chennai",
        "Rajalakshmi Institute of Technology (Autonomous)": "Chennai",
        "Rrase College of Engineering": "Kanchipuram",
        "S.A. Engineering College (Autonomous)": "Chennai",
        "SMK Fomra Institute of Technology": "Chengalpattu",
        "SRM Valliammai Engineering College (Autonomous)": "Chengalpattu",
        "Saveetha Engineering College (Autonomous)": "Chennai",
        "Sri Muthukumaran Institute of Technology": "Chennai",
        "Sri Sai Ram Engineering College (Autonomous)": "Chennai",
        "Sri Sai Ram Institute of Technology (Autonomous)": "Chennai",
        "Sri Venkateswara College of Engineering (Autonomous)": "Kanchipuram",
        "Sri Venkateswaraa College of Technology": "Chengalpattu",
        "St. Joseph College of Engineering": "Chennai",
        "St. Joseph's College of Engineering and Technology": "Thanjavur",
        "Tagore Engineering College": "Chennai",
        "Tagore Institute of Engineering and Technology": "Chengalpattu",
        "Thanthai Periyar Government Institute of Technology": "Vellore",
        "Thirumalai Engineering College": "Kanchipuram",
        "Trichy Engineering College": "Tiruchirappalli",
        "Universal College of Engineering & Technology": "Pudukkottai",
        "Vandayar Engineering College": "Thanjavur",
        "Vel Tech High Tech Dr. Rangarajan Dr. Sakunthala Engineering College (Autonomous)": "Chennai",
        "Vel Tech Multi Tech Dr. Rangarajan Dr. Sakunthala Engineering College (Autonomous)": "Chennai",
        "Velammal Engineering College (Autonomous)": "Chennai",
        "Velammal Institute of Technology": "Chennai",
    }

    for cname, dist in district_fixes.items():
        df.loc[df["college_name"] == cname, "district"] = dist

    mode_district = df.groupby("college_name")["district"].agg(
        lambda s: s.mode().iat[0] if not s.mode().empty else s.iloc[0]
    )
    df["district"] = df["college_name"].map(mode_district)

    df["district"] = df["district"].replace({"Trichy": "Tiruchirappalli"})

    assert len(df) == orig_rows
    assert list(df.columns) == [
        "college_name",
        "district",
        "tier",
        "branch",
        "category",
        "cutoff",
        "fees",
        "placement_rate",
    ]
    for col in ["cutoff", "fees", "placement_rate", "branch", "category", "tier"]:
        assert (orig[col].astype(str).values == df[col].astype(str).values).all(), (
            f"Protected column changed: {col}"
        )

    df.to_csv(OUTPUT_FILE, index=False)

    print(f"Saved {OUTPUT_FILE}")
    print(f"Rows: {len(df)}")
    print(f"Unique colleges: {df['college_name'].nunique()}")
    print(f"Unique districts: {len(df['district'].unique())}")
    print(
        "Colleges with multiple districts:",
        df.groupby("college_name")["district"].nunique().gt(1).sum(),
    )
    print(
        "Names ending slash/backslash:",
        df["college_name"].str.contains(r"[\\/]$").sum(),
    )


if __name__ == "__main__":
    main()
