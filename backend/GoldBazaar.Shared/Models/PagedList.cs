using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace GoldBazaar.Shared.Models;
public class PagedList<T> : List<T>
{
    public PaginationMetadata Meta { get; }

    public PagedList(IEnumerable<T> items, int count, int page, int size)
    {
        Meta = new(page, size, count, (int)Math.Ceiling(count / (double)size));
        AddRange(items);
    }
}
